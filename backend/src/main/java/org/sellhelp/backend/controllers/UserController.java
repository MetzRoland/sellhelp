package org.sellhelp.backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.services.EmailService;
import org.sellhelp.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final CookieGenerator cookieGenerator;


    @Autowired
    public UserController(UserService userService, EmailService emailService,
                          CookieGenerator cookieGenerator){
        this.userService = userService;
        this.emailService = emailService;
        this.cookieGenerator = cookieGenerator;
    }

    @GetMapping("/info")
    public UserDTO getUserDetails(){
        return userService.getUserDetails();
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutHandler(HttpServletRequest request, HttpServletResponse response)
    {
        String accessToken = Arrays.stream(request.getCookies()).
                filter(cookie -> cookie.getName().equals("accessToken")).toString();

        emailService.logoutUser(accessToken);

        cookieGenerator.deleteLogoutCookies(request, response);

        return ResponseEntity.ok("Sikeres kijelentkezés!");
    }

    @PatchMapping("/update/details")
    public ResponseEntity<String> updateUserDetails(@Valid @RequestBody UserDetailsUpdateDTO userDetailsUpdateDTO){
        userService.updateUserDetails(userDetailsUpdateDTO);

        return ResponseEntity.ok("Sikeres frissítés!");
    }

    @PatchMapping("/update/email")
    public ResponseEntity<String> updateUserEmail(HttpServletResponse response, @Valid @RequestBody EmailUpdateDTO emailUpdateDTO){
        TokenDTO tokenDTO = userService.updateUserEmail(emailUpdateDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok("Email sikeresen frissítve!");
    }

    @GetMapping("/update/password/send")
    public ResponseEntity<String> sendUserPasswordEmail(){
        emailService.updatePassword();

        return ResponseEntity.ok("Email a jelszó módosításhoz elküldve!");
    }

    @PatchMapping("/update/password")
    public ResponseEntity<String> updateUserPassword(HttpServletResponse response, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO){
        TokenDTO tokenDTO = userService.updateUserPassword(passwordUpdateDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok("Sikeres frissítés!");
    }
}