package org.sellhelp.backend.controllers;

import org.sellhelp.backend.dtos.requests.CreateUserDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.Role;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.RoleRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class TestRepoController {
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public TestRepoController(CityRepository cityRepository, UserRepository userRepository,
                              PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/cities")
    public List<City> getCities() {
        return cityRepository.findAll();
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping("adduser")
    public ResponseEntity<User> addUser(@RequestBody CreateUserDTO dto) {
        City city = cityRepository.findByCityName(dto.getCity().getCityName()).get();

        Role role = roleRepository.findByRoleName(dto.getRole().getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .firstName(dto.getFirst_name())
                .lastName(dto.getLast_name())
                .birthDate(dto.getBirth_date())
                .email(dto.getEmail())
                .banned(false)
                .city(city)
                .role(role)
                .build();

        UserSecret userSecret = UserSecret.builder()
                .password(passwordEncoder.encode(dto.getPassword()))
                .user(user)
                .build();
        
        user.setUserSecret(userSecret);

        userRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }


}
