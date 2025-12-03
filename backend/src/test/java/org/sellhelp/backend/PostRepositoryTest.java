package org.sellhelp.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.entities.*;
import org.sellhelp.backend.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@DataJpaTest
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;

    private Post testPost;

    @BeforeEach
    public void init(){
        County county = County.builder()
                .countyName("Baranya")
                .build();

        City city = City.builder()
                .cityName("Pécs")
                .county(county)
                .build();

        User postPublisher = User.builder()
                .firstName("Lajos")
                .lastName("Gabor")
                .email("lajosgabor@gmail.com")
                .username("lajosgabor")
                .banned(false)
                .birthDate(LocalDate.of(2001, 3, 23))
                .build();

        PostStatus postStatus = PostStatus.builder()
                .statusName("Done")
                .build();

        PostFile postFile1 = PostFile.builder()
                .postFilePath("postFile1.docx")
                .build();

        List<PostFile> postFiles = new ArrayList<>(List.of(postFile1));

        testPost = Post.builder()
                .title("Post 1")
                .description("This is post 1")
                .reward(10000)
                .city(city)
                .postPublisher(postPublisher)
                .postStatus(postStatus)
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
        assertEquals("Pécs", savedPost.getCity().getCityName());
        assertEquals("Baranya", savedPost.getCity().getCounty().getCountyName());
        assertEquals("Done", savedPost.getPostStatus().getStatusName());
        assertEquals(1, savedPost.getPostFiles().size());
        assertEquals("Lajos", savedPost.getPostPublisher().getFirstName());
    }

    @Test
    public void postCanBeUpdatedToPostRepositoryAndDB(){
        Post savedPost = postRepository.save(testPost);

        savedPost.setReward(8000);
        savedPost.getPostPublisher().setFirstName("Péter");

        Post updatedPost = postRepository.save(savedPost);

        assertNotNull(updatedPost.getId());

        assertEquals("Post 1", updatedPost.getTitle());
        assertEquals("This is post 1", updatedPost.getDescription());
        assertEquals(8000, updatedPost.getReward());
        assertEquals("Pécs", updatedPost.getCity().getCityName());
        assertEquals("Baranya", updatedPost.getCity().getCounty().getCountyName());
        assertEquals("Done", updatedPost.getPostStatus().getStatusName());
        assertEquals(1, updatedPost.getPostFiles().size());
        assertEquals("Péter", updatedPost.getPostPublisher().getFirstName());
    }

    @Test
    public void postCanBeDeletedFromPostRepositoryAndDB(){
        Post savedPost = postRepository.save(testPost);
        Integer savedPostId = savedPost.getId();

        postRepository.delete(savedPost);

        assertFalse(null, postRepository.findById(savedPostId).isPresent());
    }

    @Test
    public void postGeneralCRUDFunctionalityTest(){
        Post savedPost = postRepository.save(testPost);

        Integer savedPostId = savedPost.getId();

        assertNotNull(savedPostId);

        assertEquals("Post 1", postRepository.findById(savedPostId).get().getTitle());
        assertEquals("This is post 1", postRepository.findById(savedPostId).get().getDescription());
        assertEquals(10000, postRepository.findById(savedPostId).get().getReward());
        assertEquals("Pécs", postRepository.findById(savedPostId).get().getCity().getCityName());
        assertEquals("Baranya", postRepository.findById(savedPostId).get().getCity().getCounty().getCountyName());
        assertEquals("Done", postRepository.findById(savedPostId).get().getPostStatus().getStatusName());
        assertEquals(1, postRepository.findById(savedPostId).get().getPostFiles().size());
        assertEquals("Lajos", postRepository.findById(savedPostId).get().getPostPublisher().getFirstName());

        savedPost.getCity().setCityName("Mohács");
        savedPost.setReward(15000);

        Post updatedPost = postRepository.save(savedPost);

        assertEquals(15000, updatedPost.getReward());
        assertEquals("Mohács", updatedPost.getCity().getCityName());

        postRepository.delete(updatedPost);

        assertFalse(null, postRepository.findById(updatedPost.getId()).isPresent());
    }
}
