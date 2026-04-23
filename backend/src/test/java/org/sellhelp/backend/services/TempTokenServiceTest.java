package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TempTokenServiceTest {

    private TempTokenService tempTokenService;

    @BeforeEach
    void init() {
        tempTokenService = new TempTokenService();
    }

    @Test
    @DisplayName("Creating a token should store it and allow validation")
    void create_shouldReturnTokenAndStoreIt() {
        String username = "test@example.com";

        String token = tempTokenService.create(username);

        assertNotNull(token, "Token should not be null");
        assertTrue(tempTokenService.validate(token), "Token should be valid after creation");
        assertEquals(username, tempTokenService.getEmailByTempToken(token),
                "Email retrieved from token should match original username");
    }

    @Test
    @DisplayName("Validation should return false for unknown tokens")
    void validate_shouldReturnFalseForUnknownToken() {
        assertFalse(tempTokenService.validate("nonexistent-token"),
                "Unknown token should not be valid");
    }

    @Test
    @DisplayName("Removing a token should invalidate it")
    void removeToken_shouldInvalidateToken() {
        String username = "test@example.com";
        String token = tempTokenService.create(username);

        assertTrue(tempTokenService.validate(token), "Token should initially be valid");

        tempTokenService.removeToken(token);

        assertFalse(tempTokenService.validate(token), "Token should be invalid after removal");
        assertNull(tempTokenService.getEmailByTempToken(token),
                "Email retrieved from removed token should be null");
    }

    @Test
    @DisplayName("Retrieving email for unknown token should return null")
    void getEmailByTempToken_shouldReturnNullForUnknownToken() {
        assertNull(tempTokenService.getEmailByTempToken("nonexistent-token"),
                "Unknown token should return null for email");
    }
}