package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.County;
import org.sellhelp.backend.repositories.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@DataJpaTest
public class CityRepositoryTest {
    @Autowired
    private CityRepository cityRepository;

    private City testCity;

    @BeforeEach
    public void init(){
        County county = County.builder()
                .countyName("Baranya")
                .build();

        testCity = City.builder()
                .cityName("Pécs")
                .county(county)
                .build();

    }

    @Test
    public void cityCanBeAddedToCityRepositoryAndDB(){
        City savedCity = cityRepository.save(testCity);

        assertNotNull(savedCity.getId());

        assertEquals("Pécs", savedCity.getCityName());
        assertEquals("Baranya", savedCity.getCounty().getCountyName());
    }

    @Test
    public void cityCanBeUpdatedToCityRepositoryAndDB(){
        City savedCity = cityRepository.save(testCity);

        savedCity.setCityName("Budapest");
        savedCity.getCounty().setCountyName("Pest");

        City updatedCity = cityRepository.save(savedCity);

        assertNotNull(updatedCity.getId());

        assertEquals("Budapest", updatedCity.getCityName());
        assertEquals("Pest", updatedCity.getCounty().getCountyName());
    }

    @Test
    public void cityCanBeDeletedFromCityRepositoryAndDB(){
        City savedCity = cityRepository.save(testCity);
        Integer savedCityId = savedCity.getId();

        cityRepository.delete(savedCity);

        assertFalse(null, cityRepository.findById(savedCityId).isPresent());
    }

    @Test
    public void cityGeneralCRUDFunctionalityTest(){
        City savedCity = cityRepository.save(testCity);
        Integer savedCityId = savedCity.getId();

        assertNotNull(savedCityId);

        assertEquals("Pécs", cityRepository.findById(savedCityId).get().getCityName());
        assertEquals("Baranya", cityRepository.findById(savedCityId).get().getCounty().getCountyName());

        savedCity.setCityName("Budapest");
        savedCity.getCounty().setCountyName("Pest");

        City updatedCity = cityRepository.save(savedCity);

        assertNotNull(updatedCity.getId());

        assertEquals("Budapest", updatedCity.getCityName());
        assertEquals("Pest", updatedCity.getCounty().getCountyName());

        cityRepository.delete(updatedCity);

        assertFalse(null, cityRepository.findById(updatedCity.getId()).isPresent());
    }
}
