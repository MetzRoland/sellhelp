package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.UserDetailsUpdateDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CityRepository cityRepository;


    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper,
                       CityRepository cityRepository)
    {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.cityRepository = cityRepository;
    }

    public void updateUserDetails(String email, UserDetailsUpdateDTO userDetailsUpdateDTO)
    {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("A felhasználó nem található!"));

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

    public UserDTO getUserDetails(String email)
    {
        UserDTO userDTO = modelMapper.map(userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("A felhasználó nem található!")), UserDTO.class);
        return userDTO;
    }
}
