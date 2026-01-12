package org.sellhelp.backend.repositories;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Review;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.enums.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private User testSenderUser;
    private User testReviewedUser;
    private Review testReview;

    @BeforeEach
    void init() {
        testSenderUser = userRepository.save(User.builder()
                .firstName("Kis")
                .lastName("Béla")
                .birthDate(LocalDate.of(2001, 11, 3))
                .email("kB@newMail.mail")
                .authProvider(AuthProvider.LOCAL)
                .build());

        testReviewedUser = userRepository.save(User.builder()
                .firstName("Roland")
                .lastName("Metz")
                .birthDate(LocalDate.of(2003, 5, 12))
                .email("a@gmail.com")
                .authProvider(AuthProvider.LOCAL)
                .build());

        testReview = Review.builder()
                .senderUser(testSenderUser)
                .reviewedUser(testReviewedUser)
                .rating((byte) 5)
                .comment("Nagyon segítőkész!")
                .build();
    }

    @Test
    void reviewCanBeSavedToReviewRepositoryAndDB() {
        Review savedReview = reviewRepository.save(testReview);

        assertNotNull(savedReview.getId());
        assertEquals(testSenderUser.getId(), savedReview.getSenderUser().getId());
        assertEquals(testReviewedUser.getId(), savedReview.getReviewedUser().getId());
        assertEquals(testReviewedUser.getCreatedAt(), savedReview.getReviewedUser().getCreatedAt());
        assertEquals("Nagyon segítőkész!", savedReview.getComment());
        assertEquals(5, savedReview.getRating());
    }

    @Test
    void reviewCanBeUpdatedToReviewRepositoryAndDB() {
        Review savedReview = reviewRepository.save(testReview);

        savedReview.setComment("Update");
        savedReview.setRating((byte) 4);
        Review updated = reviewRepository.save(savedReview);

        assertEquals(savedReview.getId(), updated.getId());
        assertEquals("Update", updated.getComment());
        assertEquals((byte) 4, updated.getRating());
    }



    @Test
    public void reviewGeneralCRUDFunctionalityTest()
    {
        Review savedReview = reviewRepository.save(testReview);

        String originalComment = savedReview.getComment();
        byte originalRating = savedReview.getRating();

        savedReview.setComment("Updated comment");
        savedReview.setRating((byte) 3);
        Review updatedReview = reviewRepository.save(savedReview);

        assertNotEquals(originalComment, updatedReview.getComment());
        assertNotEquals(originalRating, updatedReview.getRating());

        Review test = savedReview.toBuilder().build();
        assertEquals(test, updatedReview);

        test.setComment("000");
        assertNotEquals(test, updatedReview);
    }

    @Test
    public void deleteUserWithReviewTest()
    {
        Review savedReview = reviewRepository.save(testReview);
        Integer sReviewId = savedReview.getId();

        userRepository.delete(savedReview.getSenderUser());
        savedReview.setSenderUser(null);

        assertEquals(1, userRepository.count());

    }
}
