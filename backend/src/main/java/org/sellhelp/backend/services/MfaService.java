package org.sellhelp.backend.services;

import org.sellhelp.backend.dtos.requests.FirstTotpValidationDTO;
import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
import org.sellhelp.backend.dtos.responses.GenerateTotpDTO;
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

    public GenerateTotpDTO generateMfa(){
        String email = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        if(user.getUserSecret().isMfa()){
            throw new MfaException("Már engedélyezve van a kétfaktoros hitelesítés!");
        }

        String totpSecret = totpService.generateSecret();

        String issuer = "SellHelp";
        String otpauthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                email,
                totpSecret,
                issuer
        );

        String qrBase64;
        String tempToken;
        try {
            qrBase64 = qrCodeService.generateQrBase64(otpauthUrl);
            tempToken = tempTokenService.create(email);
        } catch (Exception e) {
            throw new RuntimeException("QR kód generálása sikertelen", e);
        }

        return new GenerateTotpDTO(totpSecret, qrBase64, tempToken);
    }

    public void enableMfa(FirstTotpValidationDTO firstTotpValidationDTO){
        String email = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        if (!tempTokenService.validate(firstTotpValidationDTO.getTempToken())) {
            throw new InvalidTokenException("Helytelen temp token!");
        }

        if(!totpService.verify(firstTotpValidationDTO.getTotpSecret(), firstTotpValidationDTO.getTotpCode())){
            throw new InvalidTotpException("Helytelen hitelesítő kód!");
        }

        user.getUserSecret().setMfa(true);
        user.getUserSecret().setTotpSecret(firstTotpValidationDTO.getTotpSecret());

        tempTokenService.removeToken(firstTotpValidationDTO.getTempToken());

        emailService.mfaEnabled(email);

        userRepository.save(user);
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
