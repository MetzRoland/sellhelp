package org.sellhelp.backend.controllers;


import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserRepository userRepository, ModelMapper modelMapper){
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/info")
    public UserDTO getUserDetails(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        UserDTO userDTO = modelMapper.map(userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("A felhasználó nem található!")), UserDTO.class);

        return userDTO;
    }
}