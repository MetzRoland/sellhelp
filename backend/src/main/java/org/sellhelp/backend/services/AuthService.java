package org.sellhelp.backend.services;

import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.LoginDTO;
import org.sellhelp.backend.dtos.requests.RefreshDTO;
import org.sellhelp.backend.dtos.requests.RegisterDTO;
import org.sellhelp.backend.dtos.requests.TotpCodeDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.TotpSecretDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.Role;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.enums.AuthProvider;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.RoleRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final TotpService totpService;
    private final QrCodeService qrCodeService;
    private final TempTokenService tempTokenService;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       CityRepository cityRepository, PasswordEncoder passwordEncoder,
                       ModelMapper modelMapper, UserDetailsService userDetailsService,
                       AuthenticationManager authenticationManager, JWTUtil jwtUtil,
                       TotpService totpService, QrCodeService qrCodeService,TempTokenService tempTokenService){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.totpService = totpService;
        this.qrCodeService = qrCodeService;
        this.tempTokenService = tempTokenService;
    }

    public void registerLocalUser(RegisterDTO registerDTO){
        City city = cityRepository.findByCityName(registerDTO.getCityName()).orElseThrow(
                () -> new RuntimeException("A város nem található")
        );

        Role role = roleRepository.findByRoleName("ROLE_USER").orElseThrow(
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
}