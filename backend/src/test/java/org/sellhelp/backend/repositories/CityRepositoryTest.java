package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
public class CityRepositoryTest {
    @Autowired
    private CityRepository cityRepository;

    private City testCity;

    @BeforeEach
    public void init(){
        testCity = City.builder()
                .cityName("Pécs")
                .build();

    }

    @Test
    public void cityCanBeAddedToCityRepositoryAndDB(){
        City savedCity = cityRepository.save(testCity);

        assertNotNull(savedCity.getId());

        assertEquals("Pécs", savedCity.getCityName());
    }

    @Test
    public void cityCanBeUpdatedToCityRepositoryAndDB(){
        City savedCity = cityRepository.save(testCity);

        savedCity.setCityName("Budapest");

        City updatedCity = cityRepository.save(savedCity);

        assertNotNull(updatedCity.getId());

        assertEquals("Budapest", updatedCity.getCityName());
    }

    @Test
    public void cityCanBeDeletedFromCityRepositoryAndDB(){
        City savedCity = cityRepository.save(testCity);
        Integer savedCityId = savedCity.getId();

        cityRepository.delete(savedCity);

        assertFalse(cityRepository.findById(savedCityId).isPresent());
    }

    @Test
    public void cityGeneralCRUDFunctionalityTest(){
        City savedCity = cityRepository.save(testCity);
        Integer savedCityId = savedCity.getId();

        assertNotNull(savedCityId);

        assertEquals("Pécs", cityRepository.findById(savedCityId).get().getCityName());

        savedCity.setCityName("Budapest");

        City updatedCity = cityRepository.save(savedCity);

        assertNotNull(updatedCity.getId());

        assertEquals("Budapest", updatedCity.getCityName());

        cityRepository.delete(updatedCity);

        assertFalse(cityRepository.findById(updatedCity.getId()).isPresent());
    }
}
