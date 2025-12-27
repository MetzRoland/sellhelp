package org.sellhelp.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.dtos.requests.LoginDTO;
import org.sellhelp.backend.dtos.requests.RefreshDTO;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
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
    private RefreshDTO refreshDTO;
    private TotpCodeDTO totpCodeDTO;
    private TokenDTO tokenDTO;
    private TotpSecretDTO totpSecretDTO;

    @BeforeEach
    public void init(){
        registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@test.com");
        registerDTO.setPassword("Password1234.");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("User");
        registerDTO.setCityName("Pécs");
        registerDTO.setBirthDate(LocalDate.of(2000, 12, 31));

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("pass");

        refreshDTO = new RefreshDTO();
        refreshDTO.setRefreshToken("refreshToken");

        totpCodeDTO = new TotpCodeDTO();
        totpCodeDTO.setTotpCode("123456");

        tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken("accessToken");
        tokenDTO.setRefreshToken("refreshToken");

        totpSecretDTO = new TotpSecretDTO();
        totpSecretDTO.setTotpSecret("secretKey");
    }

    @Test
    public void authControllerRegisterUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("userRole", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(registerDTO.getFirstName())));
    }

    @Test
    void testLoginUserHandler() throws Exception {
        when(authService.userLogin(any(LoginDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginSuperUserHandler() throws Exception {
        when(authService.superUserLogin(any(LoginDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/auth/login/superuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testRefreshHandler() throws Exception {
        when(authService.refresh(any(RefreshDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/auth/login/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testEnableMfa() throws Exception {
        when(mfaService.enableMfa()).thenReturn(totpSecretDTO);

        mockMvc.perform(get("/auth/enable2fa"))
                .andExpect(status().isOk());
    }

    @Test
    void testDisableMfa() throws Exception {
        when(mfaService.disableMfa()).thenReturn(totpSecretDTO);

        mockMvc.perform(get("/auth/disable2fa"))
                .andExpect(status().isOk());
    }

    @Test
    void testVerifyTotp() throws Exception {
        when(mfaService.validateTotpCode(any(TotpCodeDTO.class))).thenReturn(tokenDTO);

        mockMvc.perform(post("/auth/verify-totp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(totpCodeDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginGoogleAuth() throws Exception {
        mockMvc.perform(get("/auth/login/google"))
                .andExpect(status().is3xxRedirection());
    }

//    @Test
//    void testHandleGoogleSuccess() throws Exception {
//        when(authService.loginRegisterByGoogleOauth2(any(OAuth2AuthenticationToken.class)))
//                .thenReturn(tokenDTO);
//
//        mockMvc.perform(get("/auth/loginSuccess")
//                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(header().string("Location", "http://localhost:3000/home"));
//    }
}
