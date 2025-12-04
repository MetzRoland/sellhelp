package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void init(){
        UserSecret userSecret = UserSecret.builder()
                .password("123")
                .lastUsedPassword(null)
                .passUpdateToken(null)
                .build();

        testUser = User.builder()
                .firstName("Roland")
                .lastName("Metz")
                .username("metzroland")
                .birthDate(LocalDate.of(2003, 5, 12))
                .email("a@gmail.com")
                .userSecret(userSecret)
                .build();

    }

    @Test
    public void userCanBeAddedToUserRepositoryAndDB(){
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());

        assertEquals("123", savedUser.getUserSecret().getPassword());
        assertEquals("Roland", savedUser.getFirstName());
        assertEquals("Metz", savedUser.getLastName());
        assertEquals("metzroland", savedUser.getUsername());
        assertEquals("a@gmail.com", savedUser.getEmail());
        assertEquals(LocalDate.of(2003, 5, 12), savedUser.getBirthDate());
    }

    @Test
    public void userCanBeUpdatedToUserRepositoryAndDB(){
        userRepository.save(testUser);

        testUser.setFirstName("Karoly");

        User updatedUser = userRepository.save(testUser);

        assertNotNull(updatedUser.getId());

        assertEquals("123", updatedUser.getUserSecret().getPassword());
        assertEquals("Karoly", updatedUser.getFirstName());
        assertEquals("Metz", updatedUser.getLastName());
        assertEquals("metzroland", updatedUser.getUsername());
        assertEquals("a@gmail.com", updatedUser.getEmail());
        assertEquals(LocalDate.of(2003, 5, 12), updatedUser.getBirthDate());
    }

    @Test
    public void userCanBeDeletedFromUserRepositoryAndDB(){
        User savedUser = userRepository.save(testUser);
        Integer savedUserId = savedUser.getId();

        userRepository.delete(savedUser);

        assertFalse(userRepository.findById(savedUserId).isPresent());
    }

    @Test
    public void userGeneralCRUDFunctionalityTest(){
        User savedUser = userRepository.save(testUser);

        Integer savedUserId = savedUser.getId();

        assertNotNull(savedUserId);

        assertEquals("123", savedUser.getUserSecret().getPassword());
        assertEquals("Roland", savedUser.getFirstName());
        assertEquals("Metz", savedUser.getLastName());
        assertEquals("metzroland", savedUser.getUsername());
        assertEquals("a@gmail.com", savedUser.getEmail());
        assertEquals(LocalDate.of(2003, 5, 12), savedUser.getBirthDate());

        savedUser.setFirstName("Márk");

        User updatedUser = userRepository.save(savedUser);

        assertEquals("Márk", updatedUser.getFirstName());

        userRepository.delete(updatedUser);

        assertFalse(userRepository.findById(updatedUser.getId()).isPresent());
    }
}
