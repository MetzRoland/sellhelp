package org.sellhelp.backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.dtos.validationGroups.ValidationOrder;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.UserNotificationManager;
import org.sellhelp.backend.services.EmailService;
import org.sellhelp.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final CookieGenerator cookieGenerator;
    private final CurrentUser currentUser;
    private final UserNotificationManager userNotificationManager;

    @Autowired
    public UserController(UserService userService, EmailService emailService,
                          CookieGenerator cookieGenerator, CurrentUser currentUser,
                          UserNotificationManager userNotificationManager){
        this.userService = userService;
        this.emailService = emailService;
        this.cookieGenerator = cookieGenerator;
        this.currentUser = currentUser;
        this.userNotificationManager = userNotificationManager;
    }

    @GetMapping("/info")
    public UserDTO getUserDetails(@CookieValue(name = "accessToken") String accessToken){
        return userService.getUserDetails(accessToken);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutHandler(HttpServletRequest request, HttpServletResponse response)
    {
        String toEmail = currentUser.getCurrentlyLoggedUserEmail();

        userNotificationManager.createNotification(currentUser.getCurrentlyLoggedUserEntity(), "Logout", "Successfully logged out!");

        emailService.logoutUser(toEmail);

        cookieGenerator.deleteLogoutCookies(request, response);

        return ResponseEntity.ok("Sikeres kijelentkezés!");
    }

    @PatchMapping("/update/details")
    public ResponseEntity<String> updateUserDetails(@Validated(ValidationOrder.class) @RequestBody UserDetailsUpdateDTO userDetailsUpdateDTO){
        userService.updateUserDetails(userDetailsUpdateDTO);

        return ResponseEntity.ok("Sikeres frissítés!");
    }

    @PatchMapping("/update/email")
    public ResponseEntity<String> updateUserEmail(HttpServletResponse response, @Validated(ValidationOrder.class) @RequestBody EmailUpdateDTO emailUpdateDTO){
        TokenDTO tokenDTO = userService.updateUserEmail(emailUpdateDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok("Email sikeresen frissítve!");
    }

    @GetMapping("/update/password/send")
    public ResponseEntity<String> sendUserPasswordEmail(){
        String toEmail = currentUser.getCurrentlyLoggedUserEmail();
        emailService.updatePassword(toEmail, false);

        return ResponseEntity.ok("Email a jelszó módosításhoz elküldve!");
    }

    @PatchMapping("/update/password")
    public ResponseEntity<String> updateUserPassword(HttpServletResponse response, @Validated(ValidationOrder.class) @RequestBody PasswordUpdateDTO passwordUpdateDTO){
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