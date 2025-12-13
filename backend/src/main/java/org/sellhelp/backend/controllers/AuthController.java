package org.sellhelp.backend.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.LoginDTO;
import org.sellhelp.backend.dtos.requests.RefreshDTO;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.security.JWTUtil;
import org.sellhelp.backend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthController {
    private final AuthService authService;
    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(AuthService authService, JWTUtil jwtUtil, AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService){
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterDTO> registerLocalUser(@Valid @RequestBody RegisterDTO registerDTO){
        authService.registerLocalUser(registerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> loginHandler(@RequestBody LoginDTO loginDTO, HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.loginHandler(loginDTO);

        Cookie accessTokenCookie = new Cookie("accessToken", tokenDTO.getAccess_token());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);
        accessTokenCookie.setAttribute("SameSite", "Strict");

        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenDTO.getRefresh_token());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(15 * 60);
        refreshTokenCookie.setAttribute("SameSite", "Strict");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/login/refresh")
    public ResponseEntity<TokenDTO> refreshHandler(@RequestBody RefreshDTO refreshDTO)
    {
        return ResponseEntity.ok(authService.refresh(refreshDTO));
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutHandler(@RequestBody RefreshDTO refreshDTO, HttpServletResponse response)
    {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setAttribute("SameSite", "Strict");

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setAttribute("SameSite", "Strict");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Sikeres kijelentkezés!");
    }
}
