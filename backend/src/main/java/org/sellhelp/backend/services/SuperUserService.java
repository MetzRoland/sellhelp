package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.OwnedPostResponseDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.PostRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.UserNotificationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SuperUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final UserService userService;
    private final PostRepository postRepository;
    private final UserNotificationManager userNotificationManager;

    @Autowired
    public SuperUserService(UserRepository userRepository, ModelMapper modelMapper,
                            EmailService emailService, UserService userService,
                            PostRepository postRepository, UserNotificationManager userNotificationManager){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
        this.userService = userService;
        this.postRepository = postRepository;
        this.userNotificationManager = userNotificationManager;
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public UserDTO banUser(Integer userId) {
        UserDTO userDTO = changeBanStatus(userId, true);

        userNotificationManager.createNotification(modelMapper.map(userDTO, User.class), "User banned", "User got banned successfully!");

        emailService.banUser(userRepository.findById(userId).get().getEmail());

        return userDTO;
    }

    public UserDTO unbanUser(Integer userId) {
        UserDTO userDTO = changeBanStatus(userId, false);

        userNotificationManager.createNotification(modelMapper.map(userDTO, User.class), "User enabled after ban", "User got successfully enabled after banning!");

        emailService.unbanUser(userRepository.findById(userId).get().getEmail());

        return userDTO;
    }

    private UserDTO changeBanStatus(Integer userId, boolean ban) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Felhasználó nem található"));

        String targetRole = user.getRole().getRoleName();
        boolean currentIsAdmin = hasRole("ADMIN");

        if ("ROLE_MODERATOR".equals(targetRole) && !currentIsAdmin) {
            throw new RuntimeException(
                    ban
                            ? "Csak az admin bannolhat moderátort"
                            : "Csak az admin unbannolhat moderátort"
            );
        }

        if (!"ROLE_USER".equals(targetRole) && !"ROLE_MODERATOR".equals(targetRole)) {
            throw new RuntimeException(
                    ban
                            ? "Ez a felhasználó nem bannolható"
                            : "Ez a felhasználó nem unbannolható"
            );
        }

        if (ban && user.isBanned()) {
            throw new RuntimeException("A felhasználó már bannolva van!");
        }

        if (!ban && !user.isBanned()) {
            throw new RuntimeException("A felhasználó még nincs bannolva!");
        }

        user.setBanned(ban);
        userRepository.save(user);

        UserDTO userDTO = getUserAccount(userId);
        return userDTO;
    }

    public List<UserDTO> getAllUserAccounts() {
        return userService.getAllUserAccounts().stream()
                .filter(userDTO -> "ROLE_USER".equals(userDTO.getRole())
                        || (hasRole("ADMIN") && "ROLE_MODERATOR".equals(userDTO.getRole())))
                .toList();
    }

    public UserDTO getUserAccount(Integer userId) {
        return getAllUserAccounts().stream().filter(userDTO -> Objects.equals(userDTO.getId(), userId))
                .findFirst().orElseThrow(
                        () -> new UserNotFoundException("A felhasználó nem található!")
                );
    }

    public List<OwnedPostResponseDTO> getAllPosts(){
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, OwnedPostResponseDTO.class)).toList();
    }

    public void deletePost(Integer postId){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem található!")
        );

        postRepository.delete(post);
    }

    public OwnedPostResponseDTO getPostById(Integer postId) {
        return modelMapper.map(postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem található!")
        ), OwnedPostResponseDTO.class);
    }
}
