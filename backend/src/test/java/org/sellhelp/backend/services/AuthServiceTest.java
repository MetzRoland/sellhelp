package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.*;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.Role;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.enums.AuthProvider;
import org.sellhelp.backend.enums.UserRole;
import org.sellhelp.backend.exceptions.InvalidTokenException;
import org.sellhelp.backend.exceptions.UserAlreadyExistException;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.RoleRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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
    @Mock
    private CurrentUser currentUser;

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
    @DisplayName("Register a new local user successfully")
    void registerLocalUser_success() {
        when(cityRepository.findByCityName("Pécs")).thenReturn(Optional.of(city));
        when(roleRepository.findByRoleName(UserRole.ROLE_USER.name())).thenReturn(Optional.of(role));
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded-pass");

        when(modelMapper.map(any(RegisterDTO.class), eq(User.class))).thenAnswer(invocation -> {
            RegisterDTO dto = invocation.getArgument(0);
            User user = new User();
            user.setEmail(dto.getEmail());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setAuthProvider(AuthProvider.LOCAL);
            return user;
        });

        authService.registerLocalUser(registerDTO, UserRole.ROLE_USER);

        verify(userRepository).save(any(User.class));
        verify(emailService).registerUser(
                eq(registerDTO.getEmail()),
                eq(registerDTO.getFirstName()),
                eq(registerDTO.getLastName())
        );
    }

    @Test
    @DisplayName("Registering a user with existing email throws exception")
    void registerLocalUser_existingEmail_throwsException() {
        when(userRepository.findByEmail(registerDTO.getEmail()))
                .thenReturn(Optional.of(new User()));

        when(cityRepository.findByCityName("Pécs")).thenReturn(Optional.of(city));
        when(roleRepository.findByRoleName(UserRole.ROLE_USER.name())).thenReturn(Optional.of(role));

        assertThrows(UserAlreadyExistException.class,
                () -> authService.registerLocalUser(registerDTO, UserRole.ROLE_USER));
    }

    @Test
    @DisplayName("User login succeeds without MFA enabled")
    void userLogin_success() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setRole(role);

        UserSecret secret = new UserSecret();
        secret.setMfa(false);
        secret.setTotpSecret(null);
        user.setUserSecret(secret);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(jwtUtil.generateAccessToken("test@test.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("refresh-token");

        TokenDTO tokenDTO = authService.userLogin(loginDTO);

        assertEquals("access-token", tokenDTO.getAccessToken());
        assertEquals("refresh-token", tokenDTO.getRefreshToken());
        assertNull(tokenDTO.getTempToken());

        verify(emailService).loginUser("test@test.com");
    }

    @Test
    @DisplayName("User login returns temporary token when MFA is enabled")
    void userLogin_mfaEnabled() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setRole(role);

        UserSecret secret = new UserSecret();
        secret.setMfa(true);
        secret.setTotpSecret("secret");
        user.setUserSecret(secret);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(tempTokenService.create("test@test.com")).thenReturn("temp-token");

        TokenDTO tokenDTO = authService.userLogin(loginDTO);

        assertNull(tokenDTO.getAccessToken());
        assertNull(tokenDTO.getRefreshToken());
        assertEquals("temp-token", tokenDTO.getTempToken());
    }

    @Test
    @DisplayName("Refresh token returns new access token successfully")
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
        assertNull(result.getTempToken());
    }

    @Test
    @DisplayName("Forgot password email notification sent successfully")
    void forgotUserPasswordEmailNotification_success() {
        EmailUpdateDTO dto = new EmailUpdateDTO();
        dto.setEmail("test@test.com");

        User user = new User();
        user.setEmail("test@test.com");
        user.setAuthProvider(AuthProvider.LOCAL);

        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        authService.forgotUserPasswordEmailNotification(dto);

        verify(emailService).updatePassword("test@test.com", true);
    }

    @Test
    @DisplayName("Forgot password fails when user already logged in")
    void forgotUserPasswordEmailNotification_userLoggedIn() {
        EmailUpdateDTO dto = new EmailUpdateDTO();
        dto.setEmail("test@test.com");

        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(new User());

        assertThrows(RuntimeException.class,
                () -> authService.forgotUserPasswordEmailNotification(dto));

        verify(emailService, never()).updatePassword(any(), anyBoolean());
    }

    @Test
    @DisplayName("Forgot password fails for Google users")
    void forgotUserPasswordEmailNotification_googleUser() {
        EmailUpdateDTO dto = new EmailUpdateDTO();
        dto.setEmail("test@test.com");

        User user = new User();
        user.setAuthProvider(AuthProvider.GOOGLE);

        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> authService.forgotUserPasswordEmailNotification(dto));

        verify(emailService, never()).updatePassword(any(), anyBoolean());
    }

    @Test
    @DisplayName("Forgot password update succeeds")
    void updateForgotUserPassword_success() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setToken("token");
        dto.setPassword("newPassword");

        User user = new User();
        user.setEmail("test@test.com");

        UserSecret secret = new UserSecret();
        secret.setPassword("oldEncoded");
        secret.setLastUsedPassword("olderEncoded");
        user.setUserSecret(secret);

        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(null);
        when(jwtUtil.extractEmail("token")).thenReturn("test@test.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);

        when(jwtUtil.validatePasswordUpdateToken("token", userDetails)).thenReturn(true);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("newPassword", "oldEncoded")).thenReturn(false);
        when(passwordEncoder.matches("newPassword", "olderEncoded")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNew");

        authService.updateForgotUserPassword(dto);

        verify(userRepository).save(user);
        verify(emailService).updatePasswordSuccess("test@test.com");
    }

    @Test
    @DisplayName("Forgot password update fails with invalid token")
    void updateForgotUserPassword_invalidToken() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setToken("bad-token");
        dto.setPassword("newPassword");

        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(null);
        when(jwtUtil.extractEmail("bad-token")).thenReturn("test@test.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);

        when(jwtUtil.validatePasswordUpdateToken("bad-token", userDetails)).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> authService.updateForgotUserPassword(dto));
    }

    @Test
    @DisplayName("Forgot password fails if new password equals current password")
    void updateForgotUserPassword_sameAsCurrent() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setToken("token");
        dto.setPassword("samePassword");

        User user = new User();
        user.setEmail("test@test.com");

        UserSecret secret = new UserSecret();
        secret.setPassword("encodedOld");
        secret.setLastUsedPassword("encodedOlder");
        user.setUserSecret(secret);

        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(null);
        when(jwtUtil.extractEmail("token")).thenReturn("test@test.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);

        when(jwtUtil.validatePasswordUpdateToken("token", userDetails)).thenReturn(true);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("samePassword", "encodedOld")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authService.updateForgotUserPassword(dto));
    }
}