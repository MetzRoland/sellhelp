package org.sellhelp.backend.controllers;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.security.CookieGenerator;
import org.sellhelp.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
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
        Cookie accessTokenCookie = CookieGenerator.createCookie("accessToken", null, 0);
        Cookie refreshTokenCookie = CookieGenerator.createCookie("refreshToken", null, 0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Sikeres kijelentkezés!");
    }

    @PatchMapping("/update/details")
    public ResponseEntity<String> updateUserDetails(@RequestBody UserDetailsUpdateDTO userDetailsUpdateDTO){

            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = userDetails.getUsername();

            userService.updateUserDetails(email, userDetailsUpdateDTO);

            return ResponseEntity.ok("Sikeres frissítés!");
    }

//    @PatchMapping("/update/email")
//    public ResponseEntity<String> updateUserEmail(@RequestBody UserEmailUpdateDTO userEmailUpdateDTO){
//        try
//        {
//            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            String email = userDetails.getUsername();
//
//            userService.updateUserDetails(email, userEmailUpdateDTO);
//
//            return ResponseEntity.ok("Sikeres frissítés!");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("A firssítés sikertelen. Server hiba!");
//        }
//    }
//
//    @PatchMapping("/update/password")
//    public ResponseEntity<String> updateUserPassword(@RequestBody UserPasswordUpdateDTO userPasswordUpdateDTO){
//        try
//        {
//            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            String email = userDetails.getUsername();
//
//            userService.updateUserDetails(email, userDetailsUpdateDTO);
//
//            return ResponseEntity.ok("Sikeres frissítés!");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("A firssítés sikertelen. Server hiba!");
//        }
//    }

}