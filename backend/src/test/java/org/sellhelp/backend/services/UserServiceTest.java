package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.exceptions.InvalidTokenException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private CurrentUser currentUser;
    @Mock
    private EmailService emailService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        UserSecret secret = new UserSecret();
        secret.setPassword("password");
        secret.setLastUsedPassword("oldEncodedPassword");
        user.setUserSecret(secret);
    }

    @Test
    void updateUserDetails_success() {
        UserDetailsUpdateDTO dto = new UserDetailsUpdateDTO();
        dto.setFirstName("Jane");
        dto.setBirthDate(LocalDate.of(2000, 1, 1));
        dto.setCityName("Budapest");

        City city = new City();
        city.setCityName("Budapest");

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cityRepository.findByCityName("Budapest")).thenReturn(Optional.of(city));

        userService.updateUserDetails(dto);

        assertEquals("Jane", user.getFirstName());
        assertEquals(LocalDate.of(2000, 1, 1), user.getBirthDate());
        assertEquals(city, user.getCity());

        verify(userRepository).save(user);
        verify(emailService).updateUserDetailsSuccess("test@example.com");
    }

    @Test
    void updateUserDetails_userNotFound() {
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUserDetails(new UserDetailsUpdateDTO()));
    }

    @Test
    void updateUserPassword_success() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setPassword("newPassword");
        dto.setToken("validToken");

        UserDetails userDetails = mock(UserDetails.class);

        when(currentUser.getCurrentlyLoggedUserDetails()).thenReturn(userDetails);
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(jwtUtil.validatePasswordUpdateToken("validToken", userDetails)).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("newPassword", "oldEncodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(jwtUtil.generateAccessToken("test@example.com")).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken("test@example.com")).thenReturn("refreshToken");

        TokenDTO tokenDTO = userService.updateUserPassword(dto);

        assertEquals("accessToken", tokenDTO.getAccessToken());
        assertEquals("refreshToken", tokenDTO.getRefreshToken());

        verify(userRepository).save(user);
        verify(emailService).updatePasswordSuccess("test@example.com");
    }

    @Test
    void updateUserPassword_invalidToken() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setToken("invalidToken");

        when(currentUser.getCurrentlyLoggedUserDetails()).thenReturn(mock(UserDetails.class));
        when(jwtUtil.validatePasswordUpdateToken(anyString(), any())).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> userService.updateUserPassword(dto));
    }

    @Test
    void updateUserEmail_success() {
        EmailUpdateDTO dto = new EmailUpdateDTO();
        dto.setEmail("new@example.com");

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken("new@example.com")).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken("new@example.com")).thenReturn("refreshToken");

        TokenDTO tokenDTO = userService.updateUserEmail(dto);

        assertEquals("new@example.com", user.getEmail());
        assertEquals("accessToken", tokenDTO.getAccessToken());
        assertEquals("refreshToken", tokenDTO.getRefreshToken());

        verify(userRepository).save(user);
    }

    @Test
    void getUserDetails_success() {
        UserDTO userDTO = new UserDTO();

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.getUserDetails();

        assertNotNull(result);
        verify(modelMapper).map(user, UserDTO.class);
    }
}

