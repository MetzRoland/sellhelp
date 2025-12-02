package org.sellhelp.backend.controllers;

import org.sellhelp.backend.dtos.CreateUserDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.Role;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.RoleRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
public class TestRepoController {
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TestRepoController(CityRepository cityRepository, UserRepository userRepository, RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder){
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/getcities")
    public List<City> getCities(){
        return cityRepository.findAll();
    }

    @GetMapping("/getusers")
    public List<User> getUsers(){
        return userRepository.findAll();
    }

    @PostMapping("adduser")
    public ResponseEntity<User> addUser(@RequestBody CreateUserDTO dto){
        User user = User.builder()
                .username(dto.getUsername())
                .firstName(dto.getFirst_name())
                .lastName(dto.getLast_name())
                .birthDate(dto.getBirth_date())
                .email(dto.getEmail())
                .role(dto.getRole())
                .city(dto.getCity())
                .userFiles(dto.getUserFiles())
                .reviews(dto.getReviews())
                .userNotifications(dto.getNotificationList())
                .userSecret(dto.getUserSecret())
                .banned(false)
                .build();

        user.getUserSecret().setPassword(passwordEncoder.encode(dto.getPassword()));
        user.getUserSecret().setUser(user);

        userRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
