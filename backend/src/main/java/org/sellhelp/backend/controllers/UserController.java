package org.sellhelp.backend.controllers;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserRepository userRepository, ModelMapper modelMapper){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/info")
    public UserDTO getUserDetails(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        UserDTO userDTO = modelMapper.map(userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("A felhasználó nem található!")), UserDTO.class);

        return userDTO;
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
}