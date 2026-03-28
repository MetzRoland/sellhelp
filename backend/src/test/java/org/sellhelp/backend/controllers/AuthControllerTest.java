package org.sellhelp.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.dtos.requests.*;
import org.sellhelp.backend.dtos.responses.GenerateTotpDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.TotpSecretDTO;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.security.JWTUtil;
import org.sellhelp.backend.services.AuthService;
import org.sellhelp.backend.services.MfaService;
import org.sellhelp.backend.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MfaService mfaService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private CookieGenerator cookieGenerator;

    @MockitoBean
    private S3Service s3Service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private TotpCodeDTO totpCodeDTO;
    private FirstTotpValidationDTO firstTotpValidationDTO;
    private TokenDTO tokenDTO;
    private TotpSecretDTO totpSecretDTO;
    private GenerateTotpDTO generateTotpDTO;
    private EmailUpdateDTO emailUpdateDTO;
    private PasswordUpdateDTO passwordUpdateDTO;

    @BeforeEach
    void init() {
        registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@test.com");
        registerDTO.setPassword("Password1234.");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("User");
        registerDTO.setCityName("Pécs");
        registerDTO.setBirthDate(LocalDate.of(2000, 12, 31));

        emailUpdateDTO = new EmailUpdateDTO();
        emailUpdateDTO.setEmail("test@test.com");

        passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setToken("token");
        passwordUpdateDTO.setPassword("NewPassword123.");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("pass");

        totpCodeDTO = new TotpCodeDTO();
        totpCodeDTO.setTotpCode("123456");
        totpCodeDTO.setTempToken("tempToken");

        firstTotpValidationDTO = new FirstTotpValidationDTO();
        firstTotpValidationDTO.setTempToken("tempToken");
        firstTotpValidationDTO.setTotpCode("123456");
        firstTotpValidationDTO.setTotpSecret("SECRET");

        tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken("accessToken");
        tokenDTO.setRefreshToken("refreshToken");

        totpSecretDTO = new TotpSecretDTO(false, null, null);

        generateTotpDTO = new GenerateTotpDTO("SECRET", "QR_CODE", "tempToken");
    }

    @Test
    @DisplayName("Register a new user with role ROLE_USER and return created status with email")
    void authControllerRegisterUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("userRole", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(registerDTO.getEmail()));
    }

    @Test
    @DisplayName("Login a regular user and return access and refresh tokens")
    void testLoginUserHandler() throws Exception {
        when(authService.userLogin(any(LoginDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login a superuser and return access and refresh tokens")
    void testLoginSuperUserHandler() throws Exception {
        when(authService.superUserLogin(any(LoginDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/auth/login/superuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Refresh JWT token using refresh token cookie")
    void testRefreshHandler() throws Exception {
        when(authService.refresh(anyString())).thenReturn(tokenDTO);

        mockMvc.perform(get("/auth/login/refresh")
                        .cookie(new Cookie("refreshToken", "refreshToken")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Generate TOTP for 2FA setup and return secret, QR code, and temporary token")
    void testSetupMfa() throws Exception {
        when(mfaService.generateMfa()).thenReturn(generateTotpDTO);

        mockMvc.perform(get("/auth/setup2fa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totpSecret").value("SECRET"))
                .andExpect(jsonPath("$.qrCode").value("QR_CODE"))
                .andExpect(jsonPath("$.tempToken").value("tempToken"));
    }

    @Test
    @DisplayName("Enable MFA with first-time TOTP validation")
    void testEnableMfa() throws Exception {
        mockMvc.perform(post("/auth/enable2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstTotpValidationDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Disable MFA and return TotpSecretDTO")
    void testDisableMfa() throws Exception {
        when(mfaService.disableMfa()).thenReturn(totpSecretDTO);

        mockMvc.perform(get("/auth/disable2fa"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Verify TOTP code and return access and refresh tokens")
    void testVerifyTotp() throws Exception {
        when(mfaService.validateTotpCode(any(TotpCodeDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/auth/verify-totp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(totpCodeDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Redirect user to Google authentication login page")
    void testLoginGoogleAuth() throws Exception {
        mockMvc.perform(get("/auth/login/google"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Send forgot password email successfully")
    void testGetForgotPasswordEmail() throws Exception {

        mockMvc.perform(patch("/auth/forgotPasswordEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Jelszóhejreállító email elküldve!"));
    }

    @Test
    @DisplayName("Update forgotten password successfully")
    void testUpdateForgotPassword() throws Exception {

        mockMvc.perform(patch("/auth/updateForgotPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Jelszó sikeresen módosítva!"));
    }
}