package org.sellhelp.backend.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.LoginDTO;
import org.sellhelp.backend.dtos.requests.RefreshDTO;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
import org.sellhelp.backend.dtos.responses.GeneralErrorDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.TotpSecretDTO;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.services.AuthService;
import org.sellhelp.backend.services.MfaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/auth")
@RestController
public class AuthController {
    private final AuthService authService;
    private final MfaService mfaService;

    @Value("${jwt.cookie.access.time}")
    private int accessTokenCookieExpiration;

    @Value("${jwt.cookie.refresh.time}")
    private int refreshTokenCookieExpiration;

    @Autowired
    public AuthController(AuthService authService, MfaService mfaService){
        this.authService = authService;
        this.mfaService = mfaService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterDTO> registerLocalUser(@Valid @RequestBody RegisterDTO registerDTO){
        authService.registerLocalUser(registerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> loginHandler(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.loginHandler(loginDTO);

        Cookie accessTokenCookie = CookieGenerator.createCookie("accessToken", tokenDTO.getAccessToken(), accessTokenCookieExpiration);
        Cookie refreshTokenCookie = CookieGenerator.createCookie("refreshToken", tokenDTO.getRefreshToken(), refreshTokenCookieExpiration);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/login/refresh")
    public ResponseEntity<TokenDTO> refreshHandler(@Valid @RequestBody RefreshDTO refreshDTO, HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.refresh(refreshDTO);

        Cookie accessTokenCookie = CookieGenerator.createCookie("accessToken", tokenDTO.getAccessToken(), accessTokenCookieExpiration);

        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(tokenDTO);
    }

    @GetMapping("/enable2fa")
    public ResponseEntity<TotpSecretDTO> enableMfa(){
        TotpSecretDTO totpSecretDTO = mfaService.enableMfa();

        return ResponseEntity.ok(totpSecretDTO);
    }

    @GetMapping("/disable2fa")
    public ResponseEntity<TotpSecretDTO> disableMfa(){
        TotpSecretDTO totpSecretDTO = mfaService.disableMfa();

        return ResponseEntity.ok(totpSecretDTO);
    }

    @PostMapping("/verify-totp")
    public ResponseEntity<TokenDTO> verifyTotp(@Valid @RequestBody TotpCodeDTO totpCodeDTO, HttpServletResponse response){
        TokenDTO tokenDTO = mfaService.validateTotpCode(totpCodeDTO);

        Cookie accessTokenCookie = CookieGenerator.createCookie("accessToken", tokenDTO.getAccessToken(), accessTokenCookieExpiration);
        Cookie refreshTokenCookie = CookieGenerator.createCookie("refreshToken", tokenDTO.getRefreshToken(), refreshTokenCookieExpiration);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(tokenDTO);
    }
}
