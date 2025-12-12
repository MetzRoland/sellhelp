package org.sellhelp.backend.controllers;


import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/info")
    public User getUserDetails(){
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepo.findByEmail(email).get();
    }
}