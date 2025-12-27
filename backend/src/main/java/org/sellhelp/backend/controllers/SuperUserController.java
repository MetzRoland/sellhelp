package org.sellhelp.backend.controllers;

import org.sellhelp.backend.dtos.responses.SuperUserDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.services.SuperUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superuser")
public class SuperUserController {
    private final SuperUserService superUserService;

    @Autowired
    public SuperUserController(SuperUserService superUserService){
        this.superUserService = superUserService;
    }

    @GetMapping("/info")
    public SuperUserDTO getSuperUserDetails(){
        return superUserService.getSuperUserDetails();
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
