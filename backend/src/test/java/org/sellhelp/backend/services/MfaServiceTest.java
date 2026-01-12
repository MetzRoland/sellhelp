package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.TotpSecretDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.exceptions.InvalidTokenException;
import org.sellhelp.backend.exceptions.InvalidTotpException;
import org.sellhelp.backend.exceptions.MfaException;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TotpService totpService;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private TempTokenService tempTokenService;
    @Mock
    private CurrentUser currentUser;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private MfaService mfaService;

    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setEmail("test@test.com");
        user.setUserSecret(new UserSecret());
    }

    @Test
    void enableMfa_success() throws Exception {
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(totpService.generateSecret()).thenReturn("SECRET");
        when(qrCodeService.generateQrBase64(anyString())).thenReturn("QR_CODE");

        TotpSecretDTO dto = mfaService.enableMfa();

        assertTrue(dto.isMfa());
        assertEquals("SECRET", dto.getTotpSecret());
        assertEquals("QR_CODE", dto.getQrCode());

        verify(userRepository).save(user);
        verify(emailService).mfaEnabled("test@test.com");
    }

    @Test
    void enableMfa_alreadyEnabled_throws() {
        user.getUserSecret().setMfa(true);
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(MfaException.class, () -> mfaService.enableMfa());
    }

    @Test
    void validateTotpCode_success() {
        TotpCodeDTO totpCodeDTO = new TotpCodeDTO();
        totpCodeDTO.setTempToken("temp123");
        totpCodeDTO.setTotpCode("123456");

        user.getUserSecret().setTotpSecret("SECRET");

        when(tempTokenService.validate("temp123")).thenReturn(true);
        when(tempTokenService.getEmailByTempToken("temp123")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(totpService.verify("SECRET", "123456")).thenReturn(true);
        when(jwtUtil.generateAccessToken("test@test.com")).thenReturn("ACCESS");
        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("REFRESH");

        TokenDTO tokenDTO = mfaService.validateTotpCode(totpCodeDTO);

        assertEquals("ACCESS", tokenDTO.getAccessToken());
        assertEquals("REFRESH", tokenDTO.getRefreshToken());

        verify(tempTokenService).removeToken("temp123");
        verify(emailService).loginUser("test@test.com");
    }

    @Test
    void validateTotpCode_invalidToken_throws() {
        TotpCodeDTO totpCodeDTO = new TotpCodeDTO();
        totpCodeDTO.setTempToken("temp123");

        when(tempTokenService.validate("temp123")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> mfaService.validateTotpCode(totpCodeDTO));
    }

    @Test
    void validateTotpCode_invalidTotp_throws() {
        TotpCodeDTO totpCodeDTO = new TotpCodeDTO();
        totpCodeDTO.setTempToken("temp123");
        totpCodeDTO.setTotpCode("123456");

        user.getUserSecret().setTotpSecret("SECRET");

        when(tempTokenService.validate("temp123")).thenReturn(true);
        when(tempTokenService.getEmailByTempToken("temp123")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(totpService.verify("SECRET", "123456")).thenReturn(false);

        assertThrows(InvalidTotpException.class, () -> mfaService.validateTotpCode(totpCodeDTO));
    }
}

