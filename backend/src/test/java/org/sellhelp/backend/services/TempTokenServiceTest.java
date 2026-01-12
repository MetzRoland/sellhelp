package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TempTokenServiceTest {

    private TempTokenService tempTokenService;

    @BeforeEach
    void init() {
        tempTokenService = new TempTokenService();
    }

    @Test
    void create_shouldReturnTokenAndStoreIt() {
        String username = "test@example.com";

        String token = tempTokenService.create(username);

        assertNotNull(token);
        assertTrue(tempTokenService.validate(token));
        assertEquals(username, tempTokenService.getEmailByTempToken(token));
    }

    @Test
    void validate_shouldReturnFalseForUnknownToken() {
        assertFalse(tempTokenService.validate("nonexistent-token"));
    }

    @Test
    void removeToken_shouldInvalidateToken() {
        String username = "test@example.com";
        String token = tempTokenService.create(username);

        assertTrue(tempTokenService.validate(token));

        tempTokenService.removeToken(token);

        assertFalse(tempTokenService.validate(token));
        assertNull(tempTokenService.getEmailByTempToken(token));
    }

    @Test
    void getEmailByTempToken_shouldReturnNullForUnknownToken() {
        assertNull(tempTokenService.getEmailByTempToken("nonexistent-token"));
    }
}
