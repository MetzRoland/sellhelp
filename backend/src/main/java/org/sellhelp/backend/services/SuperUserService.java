package org.sellhelp.backend.services;

import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuperUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Autowired
    public SuperUserService(UserRepository userRepository, ModelMapper modelMapper,
                            EmailService emailService){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
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
        UserDTO userDTO = changeBanStatus(userId, true);

        emailService.banUser(userRepository.findById(userId).get().getEmail());

        return userDTO;
    }

    public UserDTO unbanUser(Integer userId) {
        UserDTO userDTO = changeBanStatus(userId, false);

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
        return modelMapper.map(userRepository.save(user), UserDTO.class);
    }
}
