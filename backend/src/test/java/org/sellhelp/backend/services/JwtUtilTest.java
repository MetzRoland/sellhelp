package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JWTUtil jwtUtil;

    private final String testEmail = "metzroland1111@gmail.com";
    private final String accessSecret = "accessSecret";
    private final String refreshSecret = "refreshSecret";
    private final String passwordUpdateSecret = "passwordUpdateSecret";

    @BeforeEach
    void init() throws Exception {
        jwtUtil = new JWTUtil();

        // Set private fields via reflection
        setField(jwtUtil, "access_secret", accessSecret);
        setField(jwtUtil, "refresh_secret", refreshSecret);
        setField(jwtUtil, "password_update_secret", passwordUpdateSecret);
        setField(jwtUtil, "access_time", 1000 * 60);           // 1 min
        setField(jwtUtil, "refresh_time", 1000 * 60 * 10);     // 10 min
        setField(jwtUtil, "password_update_time", 1000 * 60 * 5); // 5 min
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JWTUtil.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("Generate and validate access token")
    void jwtUtilGeneratesCorrectAccessToken() {
        String token = jwtUtil.generateAccessToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String email = jwtUtil.extractEmail(token);
        assertEquals(testEmail, email, "Extracted email should match test email");
        assertNotNull(token, "Token should not be null");

        when(userDetails.getUsername()).thenReturn(testEmail);
        assertTrue(jwtUtil.validateAccessToken(token, userDetails), "Access token should be valid");
    }

    @Test
    @DisplayName("Generate and validate refresh token")
    void jwtUtilGeneratesCorrectRefreshToken() {
        String token = jwtUtil.generateRefreshToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String email = jwtUtil.extractEmail(token);
        assertEquals(testEmail, email);
        assertNotNull(token);

        when(userDetails.getUsername()).thenReturn(testEmail);
        assertTrue(jwtUtil.validateRefreshToken(token, userDetails));
    }

    @Test
    @DisplayName("Generate and validate password update token")
    void jwtUtilGeneratesCorrectPasswordUpdateToken() {
        String token = jwtUtil.generatePasswordUpdateToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String email = jwtUtil.extractEmail(token);
        assertEquals(testEmail, email);
        assertNotNull(token);

        when(userDetails.getUsername()).thenReturn(testEmail);
        assertTrue(jwtUtil.validatePasswordUpdateToken(token, userDetails));
    }

    @Test
    @DisplayName("Invalid password update token returns false")
    void jwtUtilIncorrectPasswordUpdateTokenReturnFalse() {
        String token = jwtUtil.generatePasswordUpdateToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String incorrectToken = token.substring(1);
        assertFalse(jwtUtil.validatePasswordUpdateToken(incorrectToken, userDetails));
    }

    @Test
    @DisplayName("Invalid access token returns false")
    void jwtUtilIncorrectAccessTokenReturnFalse() {
        String token = jwtUtil.generateAccessToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String incorrectToken = token.substring(1);
        assertFalse(jwtUtil.validateAccessToken(incorrectToken, userDetails));
    }

    @Test
    @DisplayName("Invalid refresh token returns false")
    void jwtUtilIncorrectRefreshTokenReturnFalse() {
        String token = jwtUtil.generateRefreshToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String incorrectToken = token.substring(1);
        assertFalse(jwtUtil.validateRefreshToken(incorrectToken, userDetails));
    }
}