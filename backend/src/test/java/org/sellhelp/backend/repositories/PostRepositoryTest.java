package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    private User testUser;
    private Post testPost;
    private PostFile testFile;
    private Comment testComment;
    private JobApplication testJobApplication;

    @BeforeEach
    public void init() {
        UserSecret userSecret = UserSecret.builder().password("pass123").build();
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .birthDate(java.time.LocalDate.of(1990, 1, 1))
                .email("john@example.com")
                .userSecret(userSecret)
                .build();
        testUser = userRepository.save(testUser);

        testFile = PostFile.builder()
                .postFilePath("file1.pdf")
                .build();
        List<PostFile> files = new ArrayList<>();
        files.add(testFile);

        testComment = Comment.builder()
                .message("Nice post!")
                .post(testPost)
                .build();
        testComment = commentRepository.save(testComment);

        List<Comment> comments = new ArrayList<>();
        comments.add(testComment);

        testJobApplication = JobApplication.builder()
                .applicant(testUser)
                .jobPost(testPost)
                .build();
        testJobApplication = jobApplicationRepository.save(testJobApplication);

        List<JobApplication> jobApplications = new ArrayList<>();
        jobApplications.add(testJobApplication);

        testPost = Post.builder()
                .title("Test Post")
                .description("Test Description")
                .reward(5000)
                .postPublisher(testUser)
                .postFiles(files)
                .postComments(comments)
                .jobApplications(jobApplications)
                .build();
        testPost = postRepository.save(testPost);

        testFile.setPost(testPost);
    }

    @Test
    public void postCanBeAddedToRepositoryAndDB() {
        assertNotNull(testPost.getId());
        assertEquals("Test Post", testPost.getTitle());
        assertEquals(1, testPost.getPostFiles().size());
        assertEquals(testUser.getId(), testPost.getPostPublisher().getId());
        assertEquals(1, testPost.getPostComments().size());
        assertEquals(1, testPost.getJobApplications().size());
    }

    @Test
    public void postCanBeUpdatedInRepositoryAndDB() {
        testPost.setReward(8000);
        testPost.getPostFiles().clear(); // remove files

        Post updatedPost = postRepository.save(testPost);

        assertEquals(8000, updatedPost.getReward());
        assertEquals(0, updatedPost.getPostFiles().size());
    }

    @Test
    public void postCanBeDeletedFromRepositoryAndDB() {
        Integer postId = testPost.getId();

        postRepository.delete(testPost);

        assertFalse(postRepository.findById(postId).isPresent());
    }

    @Test
    public void deletingPostCascadesToFilesCommentsApplications() {
        Integer fileId = testPost.getPostFiles().get(0).getId();
        Integer commentId = testPost.getPostComments().get(0).getId();
        Integer applicationId = testPost.getJobApplications().get(0).getId();

        postRepository.delete(testPost);

        assertTrue(postRepository.findById(fileId).isEmpty(), "PostFile should be cascade removed");
        assertFalse(commentRepository.findById(commentId).isPresent(), "Comment should be cascade removed");
        assertFalse(jobApplicationRepository.findById(applicationId).isPresent(), "Application should be cascade removed");
    }

    @Test
    public void deletingPostDoesNotDeleteUser() {
        Integer userId = testUser.getId();

        postRepository.delete(testPost);

        assertTrue(userRepository.findById(userId).isPresent(), "User should not be deleted");
    }

    @Test
    public void postGeneralCRUDFunctionalityTest() {
        Post savedPost = postRepository.save(testPost);
        Integer postId = savedPost.getId();
        assertNotNull(postId, "Post ID should not be null after save");
        assertEquals("Test Post", savedPost.getTitle());
        assertEquals(1, savedPost.getPostFiles().size(), "PostFiles should be saved");
        assertEquals(1, savedPost.getPostComments().size(), "PostComments should be saved");
        assertEquals(1, savedPost.getJobApplications().size(), "Applications should be saved");
        assertEquals(testUser.getId(), savedPost.getPostPublisher().getId(), "Publisher should be correct");

        savedPost.setTitle("Updated Post Title");
        savedPost.setReward(10000);

        savedPost.getPostFiles().clear();
        savedPost.getPostComments().clear();

        Post updatedPost = postRepository.save(savedPost);

        assertEquals("Updated Post Title", updatedPost.getTitle());
        assertEquals(10000, updatedPost.getReward());
        assertEquals(0, updatedPost.getPostFiles().size(), "PostFiles should be cleared");
        assertEquals(0, updatedPost.getPostComments().size(), "PostComments should be cleared");

        Integer fileId = testFile.getId();
        Integer commentId = testComment.getId();
        Integer applicationId = testJobApplication.getId();

        postRepository.delete(updatedPost);

        assertFalse(postRepository.findById(postId).isPresent(), "Post should be deleted");

        assertTrue(postRepository.findById(fileId).isEmpty(), "PostFile should be cascade removed");
        assertFalse(commentRepository.findById(commentId).isPresent(), "Comment should be cascade removed");
        assertFalse(jobApplicationRepository.findById(applicationId).isPresent(), "Application should be cascade removed");

        assertTrue(userRepository.findById(testUser.getId()).isPresent(), "PostPublisher should not be deleted");
    }

}
