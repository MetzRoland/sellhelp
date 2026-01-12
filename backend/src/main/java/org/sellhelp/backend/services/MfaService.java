package org.sellhelp.backend.services;

import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.TotpSecretDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.exceptions.InvalidTokenException;
import org.sellhelp.backend.exceptions.InvalidTotpException;
import org.sellhelp.backend.exceptions.MfaException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MfaService {
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final QrCodeService qrCodeService;
    private final TempTokenService tempTokenService;
    private final CurrentUser currentUser;
    private final EmailService emailService;

    @Autowired
    public MfaService(JWTUtil jwtUtil, UserRepository userRepository, TotpService totpService,
                      QrCodeService qrCodeService, TempTokenService tempTokenService,
                      CurrentUser currentUser, EmailService emailService){
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.qrCodeService = qrCodeService;
        this.tempTokenService = tempTokenService;
        this.currentUser = currentUser;
        this.emailService = emailService;
    }

    public TotpSecretDTO enableMfa(){
        String email = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        if(user.getUserSecret().isMfa()){
            throw new MfaException("Már engedélyezve van a kétfaktoros hitelesítés!");
        }

        String totpSecret = totpService.generateSecret();

        user.getUserSecret().setMfa(true);
        user.getUserSecret().setTotpSecret(totpSecret);

        String issuer = "SellHelp";
        String otpauthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                email,
                totpSecret,
                issuer
        );

        String qrBase64;
        try {
            qrBase64 = qrCodeService.generateQrBase64(otpauthUrl);
        } catch (Exception e) {
            throw new RuntimeException("QR kód generálása sikertelen", e);
        }

        userRepository.save(user);

        TotpSecretDTO totpSecretDTO = new TotpSecretDTO(true, totpSecret, qrBase64);

        emailService.mfaEnabled(email);

        return totpSecretDTO;
    }

    public TotpSecretDTO disableMfa(){
        String email = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        if(!user.getUserSecret().isMfa()){
            throw new MfaException("Még nincs engedélyezve a kétfaktoros hitelesítés!");
        }

        user.getUserSecret().setMfa(false);
        user.getUserSecret().setTotpSecret(null);

        userRepository.save(user);

        TotpSecretDTO totpSecretDTO = new TotpSecretDTO(false, null, null);

        emailService.mfaDisabled(email);

        return totpSecretDTO;
    }

    public TokenDTO validateTotpCode(TotpCodeDTO totpCodeDTO){
        String tempToken = totpCodeDTO.getTempToken();

        if (!tempTokenService.validate(tempToken)) {
            throw new InvalidTokenException("Helytelen temp token!");
        }

        String email = tempTokenService.getEmailByTempToken(tempToken);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem létezik!")
        );

        if(!totpService.verify(user.getUserSecret().getTotpSecret(), totpCodeDTO.getTotpCode())){
            throw new InvalidTotpException("Helytelen hitelesítő kód!");
        }

        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        tempTokenService.removeToken(tempToken);

        TokenDTO tokenDTO = new TokenDTO(accessToken, refreshToken, null);

        emailService.loginUser(email);

        return tokenDTO;
    }
}
