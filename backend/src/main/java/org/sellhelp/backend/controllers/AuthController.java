package org.sellhelp.backend.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.*;
import org.sellhelp.backend.dtos.responses.GenerateTotpDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.TotpSecretDTO;
import org.sellhelp.backend.dtos.validationGroups.ValidationOrder;
import org.sellhelp.backend.enums.UserRole;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.services.AuthService;
import org.sellhelp.backend.services.MfaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<RegisterDTO> registerLocalUser(@Validated(ValidationOrder.class) @RequestBody RegisterDTO registerDTO,
                                                         @RequestParam(defaultValue = "ROLE_USER") UserRole userRole){
        authService.registerLocalUser(registerDTO, userRole);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> loginUserHandler(@Validated(ValidationOrder.class) @RequestBody LoginDTO loginDTO,
                                                     HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.userLogin(loginDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/login/superuser")
    public ResponseEntity<TokenDTO> loginSuperUserHandler(@Validated(ValidationOrder.class) @RequestBody LoginDTO loginDTO,
                                                          HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.superUserLogin(loginDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok(tokenDTO);
    }

    @GetMapping("/login/refresh")
    public ResponseEntity<TokenDTO> refreshHandler(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response)
    {
        TokenDTO tokenDTO = authService.refresh(refreshToken);

        cookieGenerator.refreshAccessTokenCookie(response, tokenDTO.getAccessToken());

        return ResponseEntity.ok(tokenDTO);
    }

    @GetMapping("/setup2fa")
    public ResponseEntity<GenerateTotpDTO> setupMfa(){
        GenerateTotpDTO generateTotpDTO = mfaService.generateMfa();

        return ResponseEntity.ok(generateTotpDTO);
    }

    @PostMapping("/enable2fa")
    public ResponseEntity<String> enableMfa(@Validated(ValidationOrder.class) @RequestBody FirstTotpValidationDTO firstTotpValidationDTO){
        mfaService.enableMfa(firstTotpValidationDTO);

        return ResponseEntity.ok("Kétfaktoros hitelesítés bekapcsolva!");
    }

    @GetMapping("/disable2fa")
    public ResponseEntity<TotpSecretDTO> disableMfa(){
        TotpSecretDTO totpSecretDTO = mfaService.disableMfa();

        return ResponseEntity.ok(totpSecretDTO);
    }

    @PostMapping("/verify-totp")
    public ResponseEntity<TokenDTO> verifyTotp(@Validated(ValidationOrder.class) @RequestBody TotpCodeDTO totpCodeDTO, HttpServletResponse response){
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
    public void handleGoogleSuccess(
            OAuth2AuthenticationToken auth,
            HttpServletResponse response) throws IOException {

        try {
            TokenDTO tokenDTO = authService.loginRegisterByGoogleOauth2(auth);

            if (tokenDTO.getTempToken() == null) {

                cookieGenerator.generateLoginCookies(
                        response,
                        tokenDTO.getAccessToken(),
                        tokenDTO.getRefreshToken()
                );

                response.sendRedirect("http://localhost:5173/home");

            } else {
                response.sendRedirect(
                        "http://localhost:5173/finishGoogleRegistration?tempToken="
                                + tokenDTO.getTempToken()
                );
            }

        }
        catch (Exception e) {
            response.sendRedirect("http://localhost:5173/profileInactive");
        }
    }

    @PostMapping("/google/register")
    public ResponseEntity<TokenDTO> finishGoogleRegistration(@Validated(ValidationOrder.class) @RequestBody GoogleRegisterDTO googleRegisterDTO, @RequestParam String tempToken, HttpServletResponse response){
        TokenDTO tokenDTO = authService.finishGoogleRegistration(googleRegisterDTO, tempToken);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok(tokenDTO);
    }

    @PatchMapping("/forgotPasswordEmail")
    public ResponseEntity<String> getForgotPasswordEmail(@Validated(ValidationOrder.class) @RequestBody EmailUpdateDTO emailUpdateDTO){
        authService.forgotUserPasswordEmailNotification(emailUpdateDTO);

        return ResponseEntity.ok("Jelszóhejreállító email elküldve!");
    }

    @PatchMapping("/updateForgotPassword")
    public ResponseEntity<String> updateForgotPassword(@Validated(ValidationOrder.class) @RequestBody PasswordUpdateDTO passwordUpdateDTO){
        authService.updateForgotUserPassword(passwordUpdateDTO);

        return ResponseEntity.ok("Jelszó sikeresen módosítva!");
    }
}
