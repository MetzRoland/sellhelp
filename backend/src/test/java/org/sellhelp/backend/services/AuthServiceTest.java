package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.LoginDTO;
import org.sellhelp.backend.dtos.requests.RefreshDTO;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.Role;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.enums.UserRole;
import org.sellhelp.backend.exceptions.UserAlreadyExistException;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.RoleRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private TempTokenService tempTokenService;
    @Mock
    private EmailService emailService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private City city;
    private Role role;

    @BeforeEach
    void init() {
        registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@test.com");
        registerDTO.setPassword("pass");
        registerDTO.setCityName("Pécs");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("User");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("pass");

        city = new City();
        city.setCityName("Pécs");

        role = new Role();
        role.setRoleName("ROLE_USER");
    }

    @Test
    void registerLocalUser_success() {
        when(cityRepository.findByCityName("Pécs")).thenReturn(Optional.of(city));
        when(roleRepository.findByRoleName(UserRole.ROLE_USER.name())).thenReturn(Optional.of(role));
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        when(modelMapper.map(any(RegisterDTO.class), eq(User.class))).thenAnswer(invocation -> {
            RegisterDTO dto = invocation.getArgument(0);
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            return user;
        });

        authService.registerLocalUser(registerDTO, UserRole.ROLE_USER);

        verify(userRepository, times(1)).save(any(User.class));

        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.of(
                User.builder()
                        .email(registerDTO.getEmail())
                        .firstName(registerDTO.getFirstName())
                        .lastName(registerDTO.getLastName())
                        .build()
        ));

        User user = userRepository.findByEmail(registerDTO.getEmail()).get();

        verify(emailService, times(1)).registerUser(
                eq(user.getEmail()), eq(user.getFirstName()), eq(user.getLastName())
        );
    }

    @Test
    void registerLocalUser_existingEmail_throwsException() {
        when(userRepository.findByEmail(registerDTO.getEmail()))
                .thenReturn(Optional.of(
                        User.builder()
                                .email(registerDTO.getEmail())
                                .build()
                ));

        when(cityRepository.findByCityName("Pécs")).thenReturn(Optional.of(city));
        when(roleRepository.findByRoleName(UserRole.ROLE_USER.name())).thenReturn(Optional.of(role));

        assertThrows(UserAlreadyExistException.class, () ->
                authService.registerLocalUser(registerDTO, UserRole.ROLE_USER));
    }

    @Test
    void userLogin_success() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setRole(role);

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        UserSecret secret = new UserSecret();
        secret.setMfa(false);
        secret.setPassword(passwordEncoder.encode("pass"));
        user.setUserSecret(secret);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(jwtUtil.generateAccessToken("test@test.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("refresh-token");

        TokenDTO tokenDTO = authService.userLogin(loginDTO);

        verify(authenticationManager).authenticate(argThat(token ->
                token instanceof UsernamePasswordAuthenticationToken &&
                        token.getName().equals(loginDTO.getEmail()) &&
                        token.getCredentials().equals(loginDTO.getPassword())
        ));

        assertEquals("access-token", tokenDTO.getAccessToken());
        assertEquals("refresh-token", tokenDTO.getRefreshToken());
    }

    @Test
    void userLogin_mfaEnabled() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setRole(role);

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        UserSecret secret = new UserSecret();
        secret.setMfa(true);
        secret.setPassword(passwordEncoder.encode("pass"));
        user.setUserSecret(secret);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
//        when(jwtUtil.generateAccessToken("test@test.com")).thenReturn("access-token");
//        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("refresh-token");
        when(tempTokenService.create(loginDTO.getEmail())).thenReturn("temp-token");

        TokenDTO tokenDTO = authService.userLogin(loginDTO);

        verify(authenticationManager).authenticate(argThat(token ->
                token instanceof UsernamePasswordAuthenticationToken &&
                        token.getName().equals(loginDTO.getEmail()) &&
                        token.getCredentials().equals(loginDTO.getPassword())
        ));

        assertNull(tokenDTO.getAccessToken());
        assertNull(tokenDTO.getRefreshToken());
        assertEquals("temp-token", tokenDTO.getTempToken());
    }


    @Test
    void refreshToken_success() {
        RefreshDTO refreshDTO = new RefreshDTO();
        refreshDTO.setRefreshToken("refresh-token");

        when(jwtUtil.extractEmail("refresh-token")).thenReturn("test@test.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
        when(jwtUtil.validateRefreshToken("refresh-token", userDetails)).thenReturn(true);
        when(jwtUtil.generateAccessToken("test@test.com")).thenReturn("new-access-token");

        TokenDTO result = authService.refresh(refreshDTO.getRefreshToken());

        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }
}
