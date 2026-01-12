package org.sellhelp.backend.security;

import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class UserAuthDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserAuthDetailService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        Optional<User> userRes = userRepository.findByEmail(email);

        if(userRes.isEmpty()) {
            SecurityContextHolder.clearContext();
            UserDetails deletedUser = new org.springframework.security.core.userdetails.User(
                    "deleted-user",
                    "",
                    false,  // enabled
                    false,  // accountNonExpired
                    false,  // credentialsNonExpired
                    false,  // accountNonLocked
                    Collections.emptyList()
            );
            // this will make JWTFilter reject the nonexistent user
            return deletedUser;
        }

        User user = userRes.get();

        return new UserAuthDetails(user);
    }
}