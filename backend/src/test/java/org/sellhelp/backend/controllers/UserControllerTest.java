package org.sellhelp.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTUtil;
import org.sellhelp.backend.services.EmailService;
import org.sellhelp.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private CookieGenerator cookieGenerator;
    @MockitoBean
    private CurrentUser currentUser;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private JWTUtil jwtUtil;

    private UserDTO userDTO;
    private PasswordUpdateDTO passwordUpdateDTO;
    private EmailUpdateDTO emailUpdateDTO;
    private UserDetailsUpdateDTO userDetailsUpdateDTO;

    @BeforeEach
    void init(){
        userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");

        userDetailsUpdateDTO = new UserDetailsUpdateDTO();
        userDetailsUpdateDTO.setFirstName("Jane");

        emailUpdateDTO = new EmailUpdateDTO();
        emailUpdateDTO.setEmail("new@example.com");

        passwordUpdateDTO = new PasswordUpdateDTO();
        passwordUpdateDTO.setPassword("newPassword1234.");
        passwordUpdateDTO.setToken("token");
    }

    @Test
    @DisplayName("Get user details successfully using access token")
    void getUserDetails_success() throws Exception {
        when(userService.getUserDetails("accessToken")).thenReturn(userDTO);

        mockMvc.perform(get("/user/info")
                        .cookie(new Cookie("accessToken", "accessToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService).getUserDetails("accessToken");
    }

    @Test
    @DisplayName("Logout user successfully and clear cookies")
    void logout_success() throws Exception {
        when(currentUser.getCurrentlyLoggedUserEmail())
                .thenReturn("test@example.com");

        mockMvc.perform(get("/user/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sikeres kijelentkezés!"));

        verify(emailService).logoutUser("test@example.com");
        verify(cookieGenerator).deleteLogoutCookies(any(), any());
    }

    @Test
    @DisplayName("Update user details successfully")
    void updateUserDetails_success() throws Exception {
        mockMvc.perform(patch("/user/update/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetailsUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sikeres frissítés!"));

        verify(userService).updateUserDetails(any(UserDetailsUpdateDTO.class));
    }

    @Test
    @DisplayName("Update user email successfully and refresh login cookies")
    void updateUserEmail_success() throws Exception {
        TokenDTO tokenDTO = new TokenDTO("access", "refresh", null);

        when(userService.updateUserEmail(any()))
                .thenReturn(tokenDTO);

        mockMvc.perform(patch("/user/update/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sikeresen frissítve!"));

        verify(cookieGenerator)
                .generateLoginCookies(any(), eq("access"), eq("refresh"));
    }

    @Test
    @DisplayName("Send password update email successfully")
    void sendUserPasswordEmail_success() throws Exception {
        when(currentUser.getCurrentlyLoggedUserEmail())
                .thenReturn("test@example.com");

        mockMvc.perform(get("/user/update/password/send"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email a jelszó módosításhoz elküldve!"));

        verify(emailService).updatePassword("test@example.com", false);
    }

    @Test
    @DisplayName("Update user password successfully and refresh login cookies")
    void updateUserPassword_success() throws Exception {
        TokenDTO tokenDTO = new TokenDTO("access", "refresh", null);

        when(userService.updateUserPassword(any()))
                .thenReturn(tokenDTO);

        mockMvc.perform(patch("/user/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sikeres frissítés!"));

        verify(cookieGenerator)
                .generateLoginCookies(any(), eq("access"), eq("refresh"));
    }
}