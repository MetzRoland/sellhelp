package org.sellhelp.backend.controllers;

import org.sellhelp.backend.dtos.responses.OwnedPostResponseDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.services.SuperUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @GetMapping("/users")
    public List<UserDTO> showAllUserAccounts(){
        return superUserService.getAllUserAccounts();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @GetMapping("/users/{userId}")
    public UserDTO showUserAccount(@PathVariable Integer userId){
        return superUserService.getUserAccount(userId);
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @GetMapping("/posts")
    public ResponseEntity<List<OwnedPostResponseDTO>> getAllPosts(){
        return ResponseEntity.ok(superUserService.getAllPosts());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<OwnedPostResponseDTO> getPostById(@PathVariable Integer postId){
        return ResponseEntity.ok(superUserService.getPostById(postId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @DeleteMapping("/posts/delete/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Integer postId){
        superUserService.deletePost(postId);

        return ResponseEntity.ok("Poszt törölve adminisztrátor által!");
    }
}
