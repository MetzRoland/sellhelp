package org.sellhelp.backend.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.Role;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private S3Service s3Service;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SuperUserService superUserService;

    private User user;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(1)
                .email("user@test.com")
                .banned(false)
                .role(Role.builder().roleName("ROLE_USER").build())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockRole(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                )
        );
    }

    @Test
    void banUser_success_asAdmin() {
        mockRole("ADMIN");

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setRole("ROLE_USER");

        when(userService.getAllUserAccounts())
                .thenReturn(List.of(dto));

        UserDTO result = superUserService.banUser(user.getId());

        assertTrue(user.isBanned());
        verify(userRepository).save(user);
        verify(emailService).banUser(user.getEmail());
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void banUser_alreadyBanned() {
        mockRole("ADMIN");
        user.setBanned(true);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> superUserService.banUser(user.getId()));
    }

    @Test
    void banUser_userNotFound() {
        mockRole("ADMIN");

        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> superUserService.banUser(99));
    }

    @Test
    void unbanUser_success() {
        mockRole("ADMIN");
        user.setBanned(true);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setRole("ROLE_USER");

        when(userService.getAllUserAccounts())
                .thenReturn(List.of(dto));

        UserDTO result = superUserService.unbanUser(user.getId());

        assertFalse(user.isBanned());
        verify(userRepository).save(user);
        verify(emailService).unbanUser(user.getEmail());
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void unbanUser_notBanned() {
        mockRole("ADMIN");

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> superUserService.unbanUser(user.getId()));
    }

    @Test
    void moderatorCannotBanModerator() {
        mockRole("MODERATOR");

        user.setRole(Role.builder().roleName("ROLE_MODERATOR").build());

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> superUserService.banUser(user.getId()));
    }

    @Test
    void getAllUserAccounts_asModerator_onlyUsers() {
        mockRole("MODERATOR");

        UserDTO userDto = new UserDTO();
        userDto.setRole("ROLE_USER");

        UserDTO modDto = new UserDTO();
        modDto.setRole("ROLE_MODERATOR");

        when(userService.getAllUserAccounts())
                .thenReturn(List.of(userDto, modDto));

        List<UserDTO> result = superUserService.getAllUserAccounts();

        assertEquals(1, result.size());
        assertEquals("ROLE_USER", result.get(0).getRole());
    }

    @Test
    void getAllUserAccounts_asAdmin_usersAndModerators() {
        mockRole("ADMIN");

        UserDTO userDto = new UserDTO();
        userDto.setRole("ROLE_USER");

        UserDTO modDto = new UserDTO();
        modDto.setRole("ROLE_MODERATOR");

        when(userService.getAllUserAccounts())
                .thenReturn(List.of(userDto, modDto));

        List<UserDTO> result = superUserService.getAllUserAccounts();

        assertEquals(2, result.size());
    }

    @Test
    void getUserAccount_success() {
        mockRole("ADMIN");

        UserDTO dto = new UserDTO();
        dto.setId(1);
        dto.setRole("ROLE_USER");

        when(userService.getAllUserAccounts())
                .thenReturn(List.of(dto));

        UserDTO result = superUserService.getUserAccount(1);

        assertEquals(1, result.getId());
    }

    @Test
    void getUserAccount_notFound() {
        mockRole("ADMIN");

        when(userService.getAllUserAccounts())
                .thenReturn(List.of());

        assertThrows(UserNotFoundException.class,
                () -> superUserService.getUserAccount(1));
    }
}
