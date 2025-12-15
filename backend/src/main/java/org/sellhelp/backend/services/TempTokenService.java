package org.sellhelp.backend.services;

import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class TempTokenService {

    private final Map<String, String> tokens = new HashMap<>();

    public String create(String username) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, username);
        return token;
    }

    public boolean validate(String token) {
        return tokens.containsKey(token);
    }

    public void removeToken(String token) {
        tokens.remove(token);
    }

    public String getEmailByTempToken(String token){
        return tokens.get(token);
    }
}
