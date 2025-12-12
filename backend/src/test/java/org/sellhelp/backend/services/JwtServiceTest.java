package org.sellhelp.backend.services;

import org.sellhelp.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class JwtServiceTest {

    @Autowired
    private JWTUtil jwtService;

    public void test()
    {
        String token = jwtService.generateToken("game@cucc.com");

        // jwtService.validateToken();
    }

}
