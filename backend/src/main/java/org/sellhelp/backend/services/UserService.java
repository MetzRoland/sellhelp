package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.ProfilePictureDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.enums.AuthProvider;
import org.sellhelp.backend.exceptions.InvalidTokenException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final CurrentUser currentUser;
    private final EmailService emailService;
    private final S3Service s3Service;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper,
                       CityRepository cityRepository, PasswordEncoder passwordEncoder,
                       JWTUtil jwtUtil, CurrentUser currentUser, EmailService emailService,
                       S3Service s3Service)
    {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.currentUser = currentUser;
        this.emailService = emailService;
        this.s3Service = s3Service;
    }

    public void updateUserDetails(UserDetailsUpdateDTO userDetailsUpdateDTO)
    {
        String email = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!"));

        if (userDetailsUpdateDTO.getFirstName() != null) {
            user.setFirstName(userDetailsUpdateDTO.getFirstName());
        }

        if (userDetailsUpdateDTO.getLastName() != null) {
            user.setLastName(userDetailsUpdateDTO.getLastName());
        }

        if (userDetailsUpdateDTO.getBirthDate() != null) {
            user.setBirthDate(userDetailsUpdateDTO.getBirthDate());
        }

        if (userDetailsUpdateDTO.getCityName() != null) {
            City c =  cityRepository.findByCityName(userDetailsUpdateDTO.getCityName()).orElseThrow(
                    () -> new EntityNotFoundException("A település nem található!")
            );
            user.setCity(c);
        }

        userRepository.save(user);

        emailService.updateUserDetailsSuccess(email);
    }

    public TokenDTO updateUserPassword(PasswordUpdateDTO passwordUpdateDTO)
    {
        UserDetails userDetails = currentUser.getCurrentlyLoggedUserDetails();
        String email = currentUser.getCurrentlyLoggedUserEmail();

        log.info("Email: {}", email);

        if (!jwtUtil.validatePasswordUpdateToken(passwordUpdateDTO.getToken(), userDetails)){
            throw new InvalidTokenException("A jelszómódosító token nem érvényes!");
        }

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!"));

        if(passwordEncoder.matches(passwordUpdateDTO.getPassword(), user.getUserSecret().getPassword())){
            throw new RuntimeException("Az új jelszó nem egyezhet meg a jelenlegi jelszóval!");
        }

        if(passwordEncoder.matches(passwordUpdateDTO.getPassword(), user.getUserSecret().getLastUsedPassword())){
            throw new RuntimeException("Az új jelszó nem egyezhet meg az előző jelszóval!");
        }

        user.getUserSecret().setPassword(passwordEncoder.encode(passwordUpdateDTO.getPassword()));

        userRepository.save(user);

        emailService.updatePasswordSuccess(email);

        return new TokenDTO(jwtUtil.generateAccessToken(user.getEmail()), jwtUtil.generateRefreshToken(user.getEmail()), null);
    }

    public TokenDTO updateUserEmail(EmailUpdateDTO emailUpdateDTO)
    {
        String email = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!"));

        if (emailUpdateDTO.getEmail() != null) {
            user.setEmail(emailUpdateDTO.getEmail());
        }

        userRepository.save(user);

        return new TokenDTO(jwtUtil.generateAccessToken(user.getEmail()), jwtUtil.generateRefreshToken(user.getEmail()), null);
    }

    public UserDTO getUserDetails(String accessToken)
    {
        String email = currentUser.getCurrentlyLoggedUserEmail();

        UserDTO userDTO = modelMapper.map(userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")), UserDTO.class);

        userDTO.setAccessToken(accessToken);

        return userDTO;
    }

    public List<UserDTO> getAllUserAccounts(){
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserDTO userDTO = modelMapper.map(user, UserDTO.class);

                    return userDTO;
                })
                .toList();
    }

    public UserDTO getUserAccount(Integer userId) {
        return getAllUserAccounts().stream()
                .filter(userDTO -> Objects.equals(userDTO.getId(), userId)).findFirst()
                .orElseThrow(() -> new UserNotFoundException("A felhasználó nem található!"));
    }

    public ProfilePictureDTO getUserProfilePicture(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("A felhasználó nem található!"));

        if (user.getProfilePicturePath() == null) {
            return new ProfilePictureDTO(null);
        }

        if (user.getAuthProvider() == AuthProvider.GOOGLE
                && user.getProfilePicturePath().startsWith("https://lh3.googleusercontent.com/")) {
            return new ProfilePictureDTO(user.getProfilePicturePath());
        }

        return new ProfilePictureDTO(s3Service.getDownloadURL(user.getProfilePicturePath()));
    }
}
