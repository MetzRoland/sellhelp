package org.sellhelp.backend.controllers;

import jakarta.validation.Valid;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterDTO> registerLocalUser(@Valid @RequestBody RegisterDTO registerDTO){
        authService.registerLocalUser(registerDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }
}
