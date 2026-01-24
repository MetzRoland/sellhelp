package org.sellhelp.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.security.JWTFilter;
import org.sellhelp.backend.security.UserAuthDetailService;
import org.sellhelp.backend.services.SuperUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SuperUserController.class)
@EnableMethodSecurity
@AutoConfigureMockMvc(addFilters = false)
class SuperUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuperUserService superUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserAuthDetailService userAuthDetailService;

    @MockitoBean
    private JWTFilter jwtFilter;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_asAdmin_success() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(1);
        dto.setRole("ROLE_USER");

        when(superUserService.getAllUserAccounts())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/superuser/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getAllUsers_asModerator_success() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(1);
        dto.setRole("ROLE_USER");

        when(superUserService.getAllUserAccounts())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/superuser/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_forbidden() throws Exception {
        mockMvc.perform(get("/superuser/users"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_success() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(5);

        when(superUserService.getUserAccount(5))
                .thenReturn(dto);

        mockMvc.perform(get("/superuser/users/{id}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_success() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(2);

        when(superUserService.banUser(2))
                .thenReturn(dto);

        mockMvc.perform(put("/superuser/users/ban/{id}", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void banUser_forbidden() throws Exception {
        mockMvc.perform(put("/superuser/users/ban/{id}", 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void unbanUser_success() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(3);

        when(superUserService.unbanUser(3))
                .thenReturn(dto);

        mockMvc.perform(put("/superuser/users/unban/{id}", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }
}
