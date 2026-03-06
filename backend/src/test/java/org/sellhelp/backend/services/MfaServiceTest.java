package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.dtos.requests.FirstTotpValidationDTO;
import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
import org.sellhelp.backend.dtos.responses.GenerateTotpDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock private JWTUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private TotpService totpService;
    @Mock private QrCodeService qrCodeService;
    @Mock private TempTokenService tempTokenService;
    @Mock private CurrentUser currentUser;
    @Mock private EmailService emailService;

    @InjectMocks private MfaService mfaService;

    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setEmail("test@test.com");

        UserSecret userSecret = new UserSecret();
        userSecret.setMfa(false);
        user.setUserSecret(userSecret);
    }

    @Test
    @DisplayName("Generate MFA for a user successfully")
    void generateMfa_success() {
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(totpService.generateSecret()).thenReturn("SECRET");
        when(qrCodeService.generateQrBase64(anyString())).thenReturn("QR_CODE");
        when(tempTokenService.create("test@test.com")).thenReturn("TEMP_TOKEN");

        GenerateTotpDTO result = mfaService.generateMfa();

        assertEquals("SECRET", result.getTotpSecret());
        assertEquals("QR_CODE", result.getQrCode());
        assertEquals("TEMP_TOKEN", result.getTempToken());
    }

    @Test
    @DisplayName("Generate MFA throws exception if already enabled")
    void generateMfa_alreadyEnabled_throws() {
        user.getUserSecret().setMfa(true);

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(MfaException.class, () -> mfaService.generateMfa());
    }

    @Test
    @DisplayName("Enable MFA successfully")
    void enableMfa_success() {
        FirstTotpValidationDTO dto = new FirstTotpValidationDTO();
        dto.setTempToken("TEMP_TOKEN");
        dto.setTotpSecret("SECRET");
        dto.setTotpCode("123456");

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tempTokenService.validate("TEMP_TOKEN")).thenReturn(true);
        when(totpService.verify("SECRET", "123456")).thenReturn(true);

        mfaService.enableMfa(dto);

        assertTrue(user.getUserSecret().isMfa());
        assertEquals("SECRET", user.getUserSecret().getTotpSecret());

        verify(tempTokenService).removeToken("TEMP_TOKEN");
        verify(emailService).mfaEnabled("test@test.com");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Enable MFA throws exception for invalid temp token")
    void enableMfa_invalidToken_throws() {
        FirstTotpValidationDTO dto = new FirstTotpValidationDTO();
        dto.setTempToken("TEMP_TOKEN");

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tempTokenService.validate("TEMP_TOKEN")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> mfaService.enableMfa(dto));
    }

    @Test
    @DisplayName("Enable MFA throws exception for invalid TOTP code")
    void enableMfa_invalidTotp_throws() {
        FirstTotpValidationDTO dto = new FirstTotpValidationDTO();
        dto.setTempToken("TEMP_TOKEN");
        dto.setTotpSecret("SECRET");
        dto.setTotpCode("123456");

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tempTokenService.validate("TEMP_TOKEN")).thenReturn(true);
        when(totpService.verify("SECRET", "123456")).thenReturn(false);

        assertThrows(InvalidTotpException.class, () -> mfaService.enableMfa(dto));
    }

    @Test
    @DisplayName("Disable MFA successfully")
    void disableMfa_success() {
        user.getUserSecret().setMfa(true);
        user.getUserSecret().setTotpSecret("SECRET");

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        TotpSecretDTO result = mfaService.disableMfa();

        assertFalse(result.isMfa());
        assertNull(result.getTotpSecret());
        assertNull(result.getQrCode());

        verify(userRepository).save(user);
        verify(emailService).mfaDisabled("test@test.com");
    }

    @Test
    @DisplayName("Disable MFA throws exception if not enabled")
    void disableMfa_notEnabled_throws() {
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(MfaException.class, () -> mfaService.disableMfa());
    }

    @Test
    @DisplayName("Validate TOTP code successfully")
    void validateTotpCode_success() {
        TotpCodeDTO totpCodeDTO = new TotpCodeDTO();
        totpCodeDTO.setTempToken("TEMP_TOKEN");
        totpCodeDTO.setTotpCode("123456");

        user.getUserSecret().setTotpSecret("SECRET");

        when(tempTokenService.validate("TEMP_TOKEN")).thenReturn(true);
        when(tempTokenService.getEmailByTempToken("TEMP_TOKEN")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(totpService.verify("SECRET", "123456")).thenReturn(true);
        when(jwtUtil.generateAccessToken("test@test.com")).thenReturn("ACCESS");
        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("REFRESH");

        TokenDTO tokenDTO = mfaService.validateTotpCode(totpCodeDTO);

        assertEquals("ACCESS", tokenDTO.getAccessToken());
        assertEquals("REFRESH", tokenDTO.getRefreshToken());
        assertNull(tokenDTO.getTempToken());

        verify(tempTokenService).removeToken("TEMP_TOKEN");
        verify(emailService).loginUser("test@test.com");
    }

    @Test
    @DisplayName("Validate TOTP code throws exception for invalid token")
    void validateTotpCode_invalidToken_throws() {
        TotpCodeDTO dto = new TotpCodeDTO();
        dto.setTempToken("TEMP_TOKEN");

        when(tempTokenService.validate("TEMP_TOKEN")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> mfaService.validateTotpCode(dto));
    }

    @Test
    @DisplayName("Validate TOTP code throws exception for invalid TOTP code")
    void validateTotpCode_invalidTotp_throws() {
        TotpCodeDTO dto = new TotpCodeDTO();
        dto.setTempToken("TEMP_TOKEN");
        dto.setTotpCode("123456");

        user.getUserSecret().setTotpSecret("SECRET");

        when(tempTokenService.validate("TEMP_TOKEN")).thenReturn(true);
        when(tempTokenService.getEmailByTempToken("TEMP_TOKEN")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(totpService.verify("SECRET", "123456")).thenReturn(false);

        assertThrows(InvalidTotpException.class, () -> mfaService.validateTotpCode(dto));
    }
}