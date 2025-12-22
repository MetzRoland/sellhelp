package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.EmailUpdateDTO;
import org.sellhelp.backend.dtos.requests.PasswordUpdateDTO;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.TokenDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.User;
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

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final CurrentUser currentUser;


    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper,
                       CityRepository cityRepository, PasswordEncoder passwordEncoder,
                       JWTUtil jwtUtil, CurrentUser currentUser)
    {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.currentUser = currentUser;
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
    }

    public TokenDTO updateUserPassword(PasswordUpdateDTO passwordUpdateDTO)
    {
        UserDetails userDetails = currentUser.getCurrentlyLoggedUserDetails();
        String email = currentUser.getCurrentlyLoggedUserEmail();

        if (!jwtUtil.validatePasswordUpdateToken(passwordUpdateDTO.getToken(), userDetails)){
            throw new InvalidTokenException("A token nem érvényes!");
        }

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!"));

        if (passwordUpdateDTO.getPassword() != null) {
            user.getUserSecret().setPassword(passwordEncoder.encode(passwordUpdateDTO.getPassword()));
        }

        userRepository.save(user);

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

    public UserDTO getUserDetails()
    {
        String email = currentUser.getCurrentlyLoggedUserEmail();

        UserDTO userDTO = modelMapper.map(userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")), UserDTO.class);
        return userDTO;
    }
}
