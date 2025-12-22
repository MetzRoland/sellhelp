package org.sellhelp.backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.SuperUserDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.services.EmailService;
import org.sellhelp.backend.services.SuperUserService;
import org.sellhelp.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superuser")
public class SuperUserController {
    private final SuperUserService superUserService;
    private final UserService userService;
    private final CookieGenerator cookieGenerator;
    private final EmailService emailService;

    @Autowired
    public SuperUserController(SuperUserService superUserService, UserService userService, CookieGenerator cookieGenerator,
                               EmailService emailService){
        this.superUserService = superUserService;
        this.userService = userService;
        this.cookieGenerator = cookieGenerator;
        this.emailService = emailService;
    }

    @GetMapping("/info")
    public SuperUserDTO getSuperUserDetails(){
        return superUserService.getSuperUserDetails();
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutHandler(HttpServletRequest request, HttpServletResponse response)
    {
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
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        emailService.updatePassword(email);

        return ResponseEntity.ok("Email a jelszó módosításhoz elküldve!");
    }

    @PatchMapping("/update/password")
    public ResponseEntity<String> updateUserPassword(HttpServletResponse response, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO){
        TokenDTO tokenDTO = userService.updateUserPassword(passwordUpdateDTO);

        cookieGenerator.generateLoginCookies(response, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());

        return ResponseEntity.ok("Sikeres frissítés!");
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @GetMapping("/users")
    public List<User> showAllUserAccounts(){
        return superUserService.getAllUserAccounts();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PutMapping("/users/ban/{userId}")
    public UserDTO banUser(@PathVariable Integer userId){
        return superUserService.banUser(userId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PutMapping("/users/unban/{userId}")
    public UserDTO unbanUser(@PathVariable Integer userId){
        return superUserService.unbanUser(userId);
    }
}
