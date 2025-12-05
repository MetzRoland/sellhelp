package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User testUser;
    private Post testPost;
    private Application testApplication;

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

        testApplication = Application.builder()
                .applicant(testUser)
                .jobPost(testPost)
                .build();
    }

    @Test
    public void applicantCanBeAddedToRepositoryAndDB() {
        Application savedApplication = applicationRepository.save(testApplication);

        assertNotNull(savedApplication.getId());
        assertNotNull(savedApplication.getAppliedAt());

        assertEquals(testUser.getId(), savedApplication.getApplicant().getId());
        assertEquals(testPost.getId(), savedApplication.getJobPost().getId());
    }

    @Test
    public void applicantCanBeUpdatedInRepositoryAndDB() {
        Application savedApplication = applicationRepository.save(testApplication);

        Post newPost = Post.builder()
                .title("Updated Post")
                .description("Updated Description")
                .reward(10000)
                .build();

        newPost = postRepository.save(newPost);

        savedApplication.setJobPost(newPost);

        Application updatedApplication = applicationRepository.save(savedApplication);

        assertNotNull(updatedApplication.getId());
        assertEquals(newPost.getId(), updatedApplication.getJobPost().getId());
    }

    @Test
    public void applicantCanBeDeletedFromRepositoryAndDB() {
        Application savedApplication = applicationRepository.save(testApplication);
        Integer id = savedApplication.getId();

        applicationRepository.delete(savedApplication);

        assertFalse(applicationRepository.findById(id).isPresent());
    }

    @Test
    public void applicantGeneralCRUDFunctionalityTest() {
        Application savedApplication = applicationRepository.save(testApplication);

        assertNotNull(savedApplication.getId());
        assertEquals(testUser.getId(), savedApplication.getApplicant().getId());
        assertEquals(testPost.getId(), savedApplication.getJobPost().getId());
        assertNotNull(savedApplication.getAppliedAt());

        Post newPost = Post.builder()
                .title("Another Post")
                .description("Changed")
                .reward(15000)
                .build();

        newPost = postRepository.save(newPost);

        savedApplication.setJobPost(newPost);

        Application updatedApplication = applicationRepository.save(savedApplication);

        assertEquals(newPost.getId(), updatedApplication.getJobPost().getId());

        applicationRepository.delete(updatedApplication);

        assertFalse(applicationRepository.findById(updatedApplication.getId()).isPresent());
    }

    @Test
    public void deletingApplicantDoesNotDeleteUserOrPost() {

        Integer userId = testUser.getId();
        Integer postId = testPost.getId();

        applicationRepository.delete(testApplication);

        assertTrue(userRepository.findById(userId).isPresent(),
                "User should NOT be deleted automatically");

        assertTrue(postRepository.findById(postId).isPresent(),
                "Post should NOT be deleted automatically");
    }

    @Test
    public void removingApplicantThenDeletingUserShouldSucceed() {

        applicationRepository.delete(testApplication);

        assertDoesNotThrow(() -> userRepository.delete(testUser));
    }

    @Test
    public void removingApplicantThenDeletingPostShouldSucceed() {

        applicationRepository.delete(testApplication);

        assertDoesNotThrow(() -> postRepository.delete(testPost));
    }
}
