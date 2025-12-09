package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User testUser;
    private Post testPost;
    private JobApplication testJobApplication;

    @BeforeEach
    public void init() {
        UserSecret userSecret = UserSecret.builder()
                .password("pass123")
                .build();

        testUser = User.builder()
                .firstName("Anna")
                .lastName("Kiss")
                .username("annakiss")
                .birthDate(java.time.LocalDate.of(1998, 1, 5))
                .email("anna@gmail.com")
                .userSecret(userSecret)
                .build();

        testUser = userRepository.save(testUser);

        testPost = Post.builder()
                .title("Test Job Post")
                .description("Some description")
                .reward(8000)
                .build();

        testPost = postRepository.save(testPost);

        testJobApplication = JobApplication.builder()
                .applicant(testUser)
                .jobPost(testPost)
                .build();
    }

    @Test
    public void applicantCanBeAddedToRepositoryAndDB() {
        JobApplication savedJobApplication = jobApplicationRepository.save(testJobApplication);

        assertNotNull(savedJobApplication.getId());
        assertNotNull(savedJobApplication.getAppliedAt());

        assertEquals(testUser.getId(), savedJobApplication.getApplicant().getId());
        assertEquals(testPost.getId(), savedJobApplication.getJobPost().getId());
    }

    @Test
    public void applicantCanBeUpdatedInRepositoryAndDB() {
        JobApplication savedJobApplication = jobApplicationRepository.save(testJobApplication);

        Post newPost = Post.builder()
                .title("Updated Post")
                .description("Updated Description")
                .reward(10000)
                .build();

        newPost = postRepository.save(newPost);

        savedJobApplication.setJobPost(newPost);

        JobApplication updatedJobApplication = jobApplicationRepository.save(savedJobApplication);

        assertNotNull(updatedJobApplication.getId());
        assertEquals(newPost.getId(), updatedJobApplication.getJobPost().getId());
    }

    @Test
    public void applicantCanBeDeletedFromRepositoryAndDB() {
        JobApplication savedJobApplication = jobApplicationRepository.save(testJobApplication);
        Integer id = savedJobApplication.getId();

        jobApplicationRepository.delete(savedJobApplication);

        assertFalse(jobApplicationRepository.findById(id).isPresent());
    }

    @Test
    public void applicantGeneralCRUDFunctionalityTest() {
        JobApplication savedJobApplication = jobApplicationRepository.save(testJobApplication);

        assertNotNull(savedJobApplication.getId());
        assertEquals(testUser.getId(), savedJobApplication.getApplicant().getId());
        assertEquals(testPost.getId(), savedJobApplication.getJobPost().getId());
        assertNotNull(savedJobApplication.getAppliedAt());

        Post newPost = Post.builder()
                .title("Another Post")
                .description("Changed")
                .reward(15000)
                .build();

        newPost = postRepository.save(newPost);

        savedJobApplication.setJobPost(newPost);

        JobApplication updatedJobApplication = jobApplicationRepository.save(savedJobApplication);

        assertEquals(newPost.getId(), updatedJobApplication.getJobPost().getId());

        jobApplicationRepository.delete(updatedJobApplication);

        assertFalse(jobApplicationRepository.findById(updatedJobApplication.getId()).isPresent());
    }

    @Test
    public void deletingApplicantDoesNotDeleteUserOrPost() {

        Integer userId = testUser.getId();
        Integer postId = testPost.getId();

        jobApplicationRepository.delete(testJobApplication);

        assertTrue(userRepository.findById(userId).isPresent(),
                "User should NOT be deleted automatically");

        assertTrue(postRepository.findById(postId).isPresent(),
                "Post should NOT be deleted automatically");
    }

    @Test
    public void removingApplicantThenDeletingUserShouldSucceed() {

        jobApplicationRepository.delete(testJobApplication);

        assertDoesNotThrow(() -> userRepository.delete(testUser));
    }

    @Test
    public void removingApplicantThenDeletingPostShouldSucceed() {

        jobApplicationRepository.delete(testJobApplication);

        assertDoesNotThrow(() -> postRepository.delete(testPost));
    }
}
