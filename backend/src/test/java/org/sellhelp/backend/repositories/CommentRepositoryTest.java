package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.Comment;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.enums.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private Comment testComment;
    private User testUser;
    private Post testPost;

    @BeforeEach
    public void init() {
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .authProvider(AuthProvider.LOCAL)
                .build();

        testUser = userRepository.save(testUser);

        testPost = Post.builder()
                .title("Test Post")
                .description("Post description")
                .reward(100)
                .postPublisher(testUser)
                .build();
        testPost = postRepository.save(testPost);

        testComment = Comment.builder()
                .message("This is a test comment")
                .commentPublisher(testUser)
                .post(testPost)
                .build();
    }

    @Test
    public void commentCanBeAddedToRepositoryAndDB() {
        Comment savedComment = commentRepository.save(testComment);

        assertNotNull(savedComment.getId());
        assertEquals("This is a test comment", savedComment.getMessage());
        assertEquals(testUser.getId(), savedComment.getCommentPublisher().getId());
        assertEquals(testPost.getId(), savedComment.getPost().getId());
    }

    @Test
    public void commentCanBeUpdatedInRepositoryAndDB() {
        Comment savedComment = commentRepository.save(testComment);

        savedComment.setMessage("Updated comment message");
        Comment updatedComment = commentRepository.save(savedComment);

        assertNotNull(updatedComment.getId());
        assertEquals("Updated comment message", updatedComment.getMessage());
    }

    @Test
    public void commentCanBeDeletedFromRepositoryAndDB() {
        Comment savedComment = commentRepository.save(testComment);
        Integer commentId = savedComment.getId();

        commentRepository.delete(savedComment);

        assertFalse(commentRepository.findById(commentId).isPresent());
    }

    @Test
    public void commentGeneralCRUDFunctionalityTest() {
        Comment savedComment = commentRepository.save(testComment);
        Integer commentId = savedComment.getId();

        assertNotNull(commentId);
        assertEquals("This is a test comment", commentRepository.findById(commentId).get().getMessage());

        savedComment.setMessage("Another message");
        Comment updatedComment = commentRepository.save(savedComment);

        assertEquals("Another message", updatedComment.getMessage());

        commentRepository.delete(updatedComment);
        assertFalse(commentRepository.findById(updatedComment.getId()).isPresent());
    }
}
