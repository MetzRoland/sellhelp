package org.sellhelp.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    public void test()
    {
        String token = jwtService.generateToken("game@cucc.com");

        jwtService.validateToken(token);
    }

}
