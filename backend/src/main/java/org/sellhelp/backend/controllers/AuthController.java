package org.sellhelp.backend.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.LoginDTO;
import org.sellhelp.backend.dtos.requests.RefreshDTO;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.TotpSecretDTO;
import org.sellhelp.backend.enums.UserRole;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.services.AuthService;
import org.sellhelp.backend.services.MfaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping("/auth")
@RestController
public class AuthController {
    private final AuthService authService;
    private final MfaService mfaService;
    private final CookieGenerator cookieGenerator;

    @Autowired
    public AuthController(AuthService authService, MfaService mfaService,
                          CookieGenerator cookieGenerator){
        this.authService = authService;
        this.mfaService = mfaService;
        this.cookieGenerator = cookieGenerator;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterDTO> registerLocalUser(@Valid @RequestBody RegisterDTO registerDTO,
                                                         @RequestParam(defaultValue = "ROLE_USER") UserRole userRole){
        authService.registerLocalUser(registerDTO, userRole);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> loginUserHandler(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.userLogin(loginDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/login/superuser")
    public ResponseEntity<TokenDTO> loginSuperUserHandler(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.superUserLogin(loginDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/login/refresh")
    public ResponseEntity<TokenDTO> refreshHandler(@Valid @RequestBody RefreshDTO refreshDTO, HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.refresh(refreshDTO);

        cookieGenerator.refreshAccessTokenCookie(response, tokenDTO.getAccessToken());

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

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok(tokenDTO);
    }

    @GetMapping("/login/google")
    public ResponseEntity<String> loginGoogleAuth(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");

        return ResponseEntity.ok("Redirecting...");
    }

    @GetMapping("/loginSuccess")
    public void handleGoogleSuccess(OAuth2AuthenticationToken oAuth2AuthenticationToken, HttpServletResponse response) throws IOException {
        TokenDTO tokenDTO = authService.loginRegisterByGoogleOauth2(oAuth2AuthenticationToken);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        response.sendRedirect("http://localhost:3000/home");
    }
}
