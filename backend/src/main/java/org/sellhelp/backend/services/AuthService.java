package org.sellhelp.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.LoginDTO;
import org.sellhelp.backend.dtos.requests.RefreshDTO;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.Role;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.enums.AuthProvider;
import org.sellhelp.backend.enums.UserRole;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.RoleRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final TempTokenService tempTokenService;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       CityRepository cityRepository, PasswordEncoder passwordEncoder,
                       ModelMapper modelMapper, UserDetailsService userDetailsService,
                       AuthenticationManager authenticationManager, JWTUtil jwtUtil,
                       TotpService totpService, QrCodeService qrCodeService,TempTokenService tempTokenService,
                       EmailService emailService){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tempTokenService = tempTokenService;
    }

    public void registerLocalUser(RegisterDTO registerDTO, UserRole userRole){
        City city = cityRepository.findByCityName(registerDTO.getCityName()).orElseThrow(
                () -> new RuntimeException("A város nem található")
        );

        Role role = roleRepository.findByRoleName(userRole.name()).orElseThrow(
                () -> new RuntimeException("Szerepkör nem található")
        );

        User user = modelMapper.map(registerDTO, User.class);
        user.setCity(city);
        user.setRole(role);
        user.setAuthProvider(AuthProvider.LOCAL);

        UserSecret userSecret = UserSecret.builder()
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .user(user)
                .build();

        user.setUserSecret(userSecret);

        userRepository.save(user);
        // emailService.registrationSuccessEmail(user.getEmail());
    }

    public TokenDTO loginHandler(LoginDTO loginDTO)
    {
        try {
            UsernamePasswordAuthenticationToken authInputToken =
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
            authenticationManager.authenticate(authInputToken);
        }
        catch(AuthenticationException authExc){
            throw new RuntimeException("Helytelen email vagy jelszó!");
        }

        User user = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow(
                () -> new RuntimeException("A felhasználó nem található!")
        );

        if(!user.getUserSecret().isMfa() && user.getUserSecret().getTotpSecret() == null){
            String accessToken = jwtUtil.generateAccessToken(loginDTO.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(loginDTO.getEmail());

            return new TokenDTO(accessToken, refreshToken, null);
        }

        String tempToken = tempTokenService.create(loginDTO.getEmail());

        return new TokenDTO(null, null, tempToken);
    }

    public TokenDTO refresh(RefreshDTO refreshDTO)
    {
        try{
            String refreshToken = refreshDTO.getRefreshToken();
            String email = jwtUtil.extractEmail(refreshToken);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.validateRefreshToken(refreshDTO.getRefreshToken(), userDetails))
            {
                String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername());

                return new TokenDTO(accessToken, refreshDTO.getRefreshToken(), null);
            }

        } catch(AuthenticationException authExc){
            throw new RuntimeException("Helytelen refresh token!");
        }
        return null;
    }

    public TokenDTO loginRegisterByGoogleOauth2(OAuth2AuthenticationToken oAuth2AuthenticationToken){
        OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName  = oAuth2User.getAttribute("family_name");
        String picturePath  = oAuth2User.getAttribute("picture");

        log.info("Email: {}", email);
        log.info("First name: {}", firstName);
        log.info("Last name: {}", lastName);
        log.info("Picture url: {}", picturePath);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role role = roleRepository.findByRoleName("ROLE_USER")
                    .orElseThrow();

            City city = cityRepository.findByCityName("Pécs")
                    .orElseThrow();

            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setAuthProvider(AuthProvider.GOOGLE);
            newUser.setBirthDate(LocalDate.of(2000, 12, 23));
            newUser.setRole(role);
            newUser.setCity(city);
            newUser.setProfilePicturePath(picturePath);
            return userRepository.save(newUser);
        });

        user.setProfilePicturePath(picturePath);
        userRepository.save(user);

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("")
                        .authorities(user.getRole().getRoleName())
                        .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);

        return new TokenDTO(accessToken, refreshToken, null);

    }
}