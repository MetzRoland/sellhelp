package org.sellhelp.backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;

    private Post testPost;

    @BeforeEach
    public void init(){;
        PostFile postFile1 = PostFile.builder()
                .postFilePath("postFile1.docx")
                .build();

        List<PostFile> postFiles = new ArrayList<>(List.of(postFile1));

        testPost = Post.builder()
                .title("Post 1")
                .description("This is post 1")
                .reward(10000)
                .postFiles(postFiles)
                .build();

    }

    @Test
    public void postCanBeAddedToPostRepositoryAndDB(){
        Post savedPost = postRepository.save(testPost);

        assertNotNull(savedPost.getId());

        assertEquals("Post 1", savedPost.getTitle());
        assertEquals("This is post 1", savedPost.getDescription());
        assertEquals(10000, savedPost.getReward());
        assertEquals(1, savedPost.getPostFiles().size());
    }

    @Test
    public void postCanBeUpdatedToPostRepositoryAndDB(){
        Post savedPost = postRepository.save(testPost);

        savedPost.setReward(8000);

        Post updatedPost = postRepository.save(savedPost);

        assertNotNull(updatedPost.getId());

        assertEquals("Post 1", updatedPost.getTitle());
        assertEquals("This is post 1", updatedPost.getDescription());
        assertEquals(8000, updatedPost.getReward());
        assertEquals(1, updatedPost.getPostFiles().size());
    }

    @Test
    public void postCanBeDeletedFromPostRepositoryAndDB(){
        Post savedPost = postRepository.save(testPost);
        Integer savedPostId = savedPost.getId();

        postRepository.delete(savedPost);

        assertFalse(postRepository.findById(savedPostId).isPresent());
    }

    @Test
    public void postGeneralCRUDFunctionalityTest(){
        Post savedPost = postRepository.save(testPost);

        Integer savedPostId = savedPost.getId();

        assertNotNull(savedPostId);

        assertEquals("Post 1", postRepository.findById(savedPostId).get().getTitle());
        assertEquals("This is post 1", postRepository.findById(savedPostId).get().getDescription());
        assertEquals(10000, postRepository.findById(savedPostId).get().getReward());
        assertEquals(1, postRepository.findById(savedPostId).get().getPostFiles().size());

        savedPost.setReward(15000);

        Post updatedPost = postRepository.save(savedPost);

        assertEquals(15000, updatedPost.getReward());

        postRepository.delete(updatedPost);

        assertFalse(postRepository.findById(updatedPost.getId()).isPresent());
    }
}
