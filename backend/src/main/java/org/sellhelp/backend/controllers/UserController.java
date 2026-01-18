package org.sellhelp.backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.ProfilePictureDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.services.EmailService;
import org.sellhelp.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final CookieGenerator cookieGenerator;
    private final CurrentUser currentUser;

    @Autowired
    public UserController(UserService userService, EmailService emailService,
                          CookieGenerator cookieGenerator, CurrentUser currentUser){
        this.userService = userService;
        this.emailService = emailService;
        this.cookieGenerator = cookieGenerator;
        this.currentUser = currentUser;
    }

    @GetMapping("/info")
    public UserDTO getUserDetails(@CookieValue(name = "accessToken") String accessToken){
        return userService.getUserDetails(accessToken);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutHandler(HttpServletRequest request, HttpServletResponse response)
    {
        String toEmail = currentUser.getCurrentlyLoggedUserEmail();

        emailService.logoutUser(toEmail);

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
        String toEmail = currentUser.getCurrentlyLoggedUserEmail();
        emailService.updatePassword(toEmail);

        return ResponseEntity.ok("Email a jelszó módosításhoz elküldve!");
    }

    @PatchMapping("/update/password")
    public ResponseEntity<String> updateUserPassword(HttpServletResponse response, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO){
        TokenDTO tokenDTO = userService.updateUserPassword(passwordUpdateDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok("Sikeres frissítés!");
    }

    @GetMapping("/users")
    public List<UserDTO> showAllUserAccounts(){
        return userService.getAllUserAccounts();
    }

    @GetMapping("/users/{userId}")
    public UserDTO showUserAccount(@PathVariable Integer userId){
        return userService.getUserAccount(userId);
    }
}