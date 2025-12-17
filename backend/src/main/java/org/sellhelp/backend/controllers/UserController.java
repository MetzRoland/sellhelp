package org.sellhelp.backend.controllers;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.security.JWTUtil;
import org.sellhelp.backend.services.EmailService;
import org.sellhelp.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final JWTUtil jwtUtil;
    private final CookieGenerator cookieGenerator;


    @Autowired
    public UserController(UserService userService, EmailService emailService,
                          JWTUtil jwtUtil, CookieGenerator cookieGenerator){
        this.userService = userService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.cookieGenerator = cookieGenerator;
    }

    @GetMapping("/info")
    public UserDTO getUserDetails(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        return userService.getUserDetails(email);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutHandler(HttpServletResponse response)
    {
        Cookie accessTokenCookie = cookieGenerator.createCookie("accessToken", null, 0, "/");
        Cookie refreshTokenCookie = cookieGenerator.createCookie("refreshToken", null, 0, "/auth/login/refresh");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Sikeres kijelentkezés!");
    }

    @PatchMapping("/update/details")
    public ResponseEntity<String> updateUserDetails(@Valid @RequestBody UserDetailsUpdateDTO userDetailsUpdateDTO){

            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = userDetails.getUsername();

            userService.updateUserDetails(email, userDetailsUpdateDTO);

            return ResponseEntity.ok("Sikeres frissítés!");
    }

    @PatchMapping("/update/email")
    public ResponseEntity<String> updateUserEmail(HttpServletResponse response, @Valid @RequestBody EmailUpdateDTO emailUpdateDTO){

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        TokenDTO tokenDTO = userService.updateUserEmail(email, emailUpdateDTO);

        Cookie accessTokenCookie = cookieGenerator.createAccessCookie(tokenDTO.getAccessToken());
        Cookie refreshTokenCookie = cookieGenerator.createRefreshCookie(tokenDTO.getRefreshToken());

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Email sikeresen frissítve!");
    }

    @GetMapping("/update/password/send")
    public ResponseEntity<String> sendUserPasswordEmail(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        emailService.updatePassword(email);

        return ResponseEntity.ok("Email a jelszó módosításhoz elküldve!");
    }

    @PatchMapping("/update/password")
    public ResponseEntity<String> updateUserPassword(HttpServletResponse response, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO){
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = userDetails.getUsername();

            if (!jwtUtil.validatePasswordUpdateToken(passwordUpdateDTO.getToken(), userDetails))
            {return ResponseEntity.badRequest().body(null);}

            TokenDTO tokenDTO = userService.updateUserPassword(email, passwordUpdateDTO);

        Cookie accessTokenCookie = cookieGenerator.createAccessCookie(tokenDTO.getAccessToken());
        Cookie refreshTokenCookie = cookieGenerator.createRefreshCookie(tokenDTO.getRefreshToken());

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok("Sikeres frissítés!");
    }
}