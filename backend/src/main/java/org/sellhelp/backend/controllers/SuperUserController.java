package org.sellhelp.backend.controllers;

import jakarta.servlet.http.Cookie;
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
import org.sellhelp.backend.security.JWTUtil;
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
    private final JWTUtil jwtUtil;
    private final EmailService emailService;

    @Autowired
    public SuperUserController(SuperUserService superUserService, UserService userService, CookieGenerator cookieGenerator,
                               JWTUtil jwtUtil, EmailService emailService){
        this.superUserService = superUserService;
        this.userService = userService;
        this.cookieGenerator = cookieGenerator;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @GetMapping("/info")
    public SuperUserDTO getSuperUserDetails(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        return superUserService.getSuperUserDetails(email);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutHandler(HttpServletRequest request, HttpServletResponse response)
    {
        Cookie accessTokenCookie = cookieGenerator.deleteCookie("accessToken");
        Cookie refreshTokenCookie = cookieGenerator.deleteCookie("refreshToken");
        Cookie jSessionIdCookie = cookieGenerator.deleteCookie("JSESSIONID");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        response.addCookie(jSessionIdCookie);

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
