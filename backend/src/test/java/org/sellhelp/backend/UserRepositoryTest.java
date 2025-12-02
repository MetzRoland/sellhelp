package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserSecret;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.springframework.test.util.AssertionErrors.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void init(){
        testUser = User.builder()
                .firstName("Roland")
                .lastName("Metz")
                .username("metzroland")
                .birthDate(LocalDate.now())
                .email("a@gmail.com")
                .userSecret(
                        UserSecret.builder()
                                .password("123")
                                .lastUsedPassword(null)
                                .passUpdateToken(null)
                                .build()
                )
                .build();

        testUser.getUserSecret().setUser(testUser);
    }

    @Test
    public void userCanBeAddedToUserRepository(){
        userRepository.save(testUser);

        assertNotNull(null, userRepository.findById(1));
        assertEquals(null, 1, userRepository.findById(1).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getId());
        assertEquals(null, "123", userRepository.findById(1).orElseThrow(
                () -> new RuntimeException("User not found")
        ).getUserSecret().getPassword());
    }
}
