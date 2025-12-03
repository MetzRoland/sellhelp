package org.sellhelp.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Review;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    private User testSenderUser;
    private User testReviewedUser;
    private Review testReview;

    @BeforeEach
    public void init() {
        testSenderUser = User.builder()
                .username("NagyB")
                .firstName("Kis")
                .lastName("Béla")
                .birthDate(LocalDate.of(2001, 11, 03))
                .email("kB@newMail.mail")
                .build();

        testReviewedUser = User.builder()
                .username("metzroland")
                .firstName("Roland")
                .lastName("Metz")
                .birthDate(LocalDate.of(2003, 5, 12))
                .email("a@gmail.com")
                .build();

        testReview = Review.builder()
                .senderUser(testSenderUser)
                .reviewedUser(testReviewedUser)
                .rating((byte) 5)
                .comment("Nagyon segítőkész!")
                .build();
    }

    @Test
    public void reviewCanBeAddedToReviewRepositoryAndDB() {
        Review savedReview = reviewRepository.save(testReview);
        User senderUserCopy = savedReview.getSenderUser().toBuilder()
                .id(savedReview.getSenderUser().getId())
                .createdAt(savedReview.getSenderUser().getCreatedAt())
                .build();

        User reviewedUserCopy = savedReview.getReviewedUser().toBuilder()
                .id(savedReview.getReviewedUser().getId())
                .createdAt(savedReview.getReviewedUser().getCreatedAt())
                .build();

        Review reviewCopy = savedReview.toBuilder()
                .id(savedReview.getId())
                .senderUser(senderUserCopy)
                .reviewedUser(reviewedUserCopy)
                .createdAt(savedReview.getCreatedAt())
                .build();

        assertNotNull(savedReview.getId());

        assertEquals(senderUserCopy, savedReview.getSenderUser());

        assertEquals(reviewedUserCopy, savedReview.getReviewedUser());

        assertEquals(reviewCopy, savedReview);

    }

}
