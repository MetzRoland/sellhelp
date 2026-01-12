package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
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
public class JwtUtilTest {

    private JWTUtil jwtUtil;

    private final String testEmail = "metzroland1111@gmail.com";
    private final String accessSecret = "accessSecret";
    private final String refreshSecret = "refreshSecret";
    private final String passwordUpdateSecret = "passwordUpdateSecret";

    @BeforeEach
    void init() throws Exception {
        jwtUtil = new JWTUtil();

        setField(jwtUtil, "access_secret", accessSecret);
        setField(jwtUtil, "refresh_secret", refreshSecret);
        setField(jwtUtil, "password_update_secret", passwordUpdateSecret);
        setField(jwtUtil, "access_time", 1000 * 60);
        setField(jwtUtil, "refresh_time", 1000 * 60 * 10);
        setField(jwtUtil, "password_update_time", 1000 * 60 * 5);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JWTUtil.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void jwtUtilGeneratesCorrectAccessToken(){
        String token = jwtUtil.generateAccessToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String email = jwtUtil.extractEmail(token);
        assertEquals(testEmail, email);

        assertNotNull(token);

        when(userDetails.getUsername()).thenReturn(testEmail);
        assertTrue(jwtUtil.validateAccessToken(token, userDetails));
    }

    @Test
    public void jwtUtilGeneratesCorrectRefreshToken(){
        String token = jwtUtil.generateRefreshToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String email = jwtUtil.extractEmail(token);
        assertEquals(testEmail, email);

        assertNotNull(token);

        when(userDetails.getUsername()).thenReturn(testEmail);
        assertTrue(jwtUtil.validateRefreshToken(token, userDetails));
    }

    @Test
    public void jwtUtilGeneratesCorrectPasswordUpdateToken(){
        String token = jwtUtil.generatePasswordUpdateToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String email = jwtUtil.extractEmail(token);
        assertEquals(testEmail, email);

        assertNotNull(token);

        when(userDetails.getUsername()).thenReturn(testEmail);
        assertTrue(jwtUtil.validatePasswordUpdateToken(token, userDetails));
    }

    @Test
    public void jwtUtilIncorrectPasswordUpdateTokenReturnFalse(){
        String token = jwtUtil.generatePasswordUpdateToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String incorrectToken = token.substring(1);

        assertFalse(jwtUtil.validatePasswordUpdateToken(incorrectToken, userDetails));
    }

    @Test
    public void jwtUtilIncorrectAccessTokenReturnFalse(){
        String token = jwtUtil.generateAccessToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String incorrectToken = token.substring(1);

        assertFalse(jwtUtil.validateAccessToken(incorrectToken, userDetails));
    }

    @Test
    public void jwtUtilIncorrectRefreshTokenReturnFalse(){
        String token = jwtUtil.generateRefreshToken(testEmail);
        UserDetails userDetails = mock(UserDetails.class);

        String incorrectToken = token.substring(1);

        assertFalse(jwtUtil.validateRefreshToken(incorrectToken, userDetails));
    }
}
