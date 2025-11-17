package org.sellhelp.backend.controllers;

import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class TestRepoController {
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    @Autowired
    public TestRepoController(CityRepository cityRepository, UserRepository userRepository){
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/getcities")
    public List<City> getCities(){
        return cityRepository.findAll();
    }

    @GetMapping("/getusers")
    public List<User> getUsers(){
        return userRepository.findAll();
    }
}
