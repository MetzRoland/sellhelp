package org.sellhelp.backend.services;

import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.SuperUserDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.UserRepository;
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

    @Autowired
    public SuperUserService(UserRepository userRepository, ModelMapper modelMapper){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public SuperUserDTO getSuperUserDetails(String email) {
        SuperUserDTO superUserDTO = modelMapper.map(userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("A felhasználó nem található!")), SuperUserDTO.class);
        return superUserDTO;
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public List<User> getAllUserAccounts(){
        boolean admin = hasRole("ADMIN");

        return userRepository.findAll()
                .stream()
                .filter(user -> {
                    String role = user.getRole().getRoleName();
                    return "ROLE_USER".equals(role) || (admin && "ROLE_MODERATOR".equals(role));
                })
                .toList();
    }

    public UserDTO banUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Felhasználó nem található"));

        String targetRole = user.getRole().getRoleName();
        boolean currentIsAdmin = hasRole("ADMIN");

        if ("ROLE_MODERATOR".equals(targetRole) && !currentIsAdmin) {
            throw new RuntimeException("Csak az admin bannolhat moderátort");
        }

        if (!"ROLE_USER".equals(targetRole) && !"ROLE_MODERATOR".equals(targetRole)) {
            throw new RuntimeException("Ez a felhasználó nem bannolható");
        }

        if (user.isBanned()) {
            throw new RuntimeException("A felhasználó már bannolva van!");
        }

        user.setBanned(true);
        return modelMapper.map(userRepository.save(user), UserDTO.class);
    }

    public UserDTO unbanUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Felhasználó nem található"));

        String targetRole = user.getRole().getRoleName();
        boolean currentIsAdmin = hasRole("ADMIN");

        if ("ROLE_MODERATOR".equals(targetRole) && !currentIsAdmin) {
            throw new RuntimeException("Csak az admin unbannolhat moderátort");
        }

        if (!"ROLE_USER".equals(targetRole) && !"ROLE_MODERATOR".equals(targetRole)) {
            throw new RuntimeException("Ez a felhasználó nem unbannolható");
        }

        if (!user.isBanned()) {
            throw new RuntimeException("A felhasználó még nincs bannolva!");
        }

        user.setBanned(false);
        return modelMapper.map(userRepository.save(user), UserDTO.class);
    }

}
