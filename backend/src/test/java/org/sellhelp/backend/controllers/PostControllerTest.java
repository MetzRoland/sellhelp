package org.sellhelp.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.dtos.requests.*;
import org.sellhelp.backend.dtos.responses.*;
import org.sellhelp.backend.security.JWTFilter;
import org.sellhelp.backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PostService postService;

    private CreatePostDTO createPostDTO;
    private UpdatePostDTO updatePostDTO;
    private PostCommentDTO postCommentDTO;
    private ChangePostStatusDTO changePostStatusDTO;

    private PostResponseDTO postResponseDTO;
    private OwnedPostResponseDTO ownedPostResponseDTO;
    private JobApplicationResponseDTO jobApplicationResponseDTO;

    @BeforeEach
    void init() {
        createPostDTO = new CreatePostDTO();
        createPostDTO.setTitle("Post title");
        createPostDTO.setDescription("Post description");
        createPostDTO.setReward(5000);
        createPostDTO.setCityName("Budapest");

        updatePostDTO = new UpdatePostDTO();

        postCommentDTO = new PostCommentDTO();
        postCommentDTO.setMessage("Post comment");

        changePostStatusDTO = new ChangePostStatusDTO();
        changePostStatusDTO.setTargetStatusName("closed");

        postResponseDTO = new PostResponseDTO();
        ownedPostResponseDTO = new OwnedPostResponseDTO();
        jobApplicationResponseDTO = new JobApplicationResponseDTO();
    }

    @Test
    @DisplayName("Create a new post and return HTTP 201")
    void createPost_success() throws Exception {
        when(postService.createPost(any())).thenReturn(postResponseDTO);

        mockMvc.perform(post("/post/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isCreated());

        verify(postService).createPost(any(CreatePostDTO.class));
    }

    @Test
    @DisplayName("Update an existing post and return HTTP 200")
    void updatePost_success() throws Exception {
        when(postService.updatePostData(any(), eq(1)))
                .thenReturn(ownedPostResponseDTO);

        mockMvc.perform(patch("/post/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostDTO)))
                .andExpect(status().isOk());

        verify(postService).updatePostData(any(UpdatePostDTO.class), eq(1));
    }

    @Test
    @DisplayName("Delete a post by ID and return success message")
    void deletePost_success() throws Exception {
        mockMvc.perform(delete("/post/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("A poszt törölve!"));

        verify(postService).deletePost(1);
    }

    @Test
    @DisplayName("Retrieve all available posts and return HTTP 200")
    void getAvailablePosts_success() throws Exception {
        when(postService.getAvailablePosts())
                .thenReturn(List.of(postResponseDTO));

        mockMvc.perform(get("/post/posts"))
                .andExpect(status().isOk());

        verify(postService).getAvailablePosts();
    }

    @Test
    @DisplayName("Retrieve posts the user is involved in and return HTTP 200")
    void getInvolvedPosts_success() throws Exception {
        when(postService.getInvolvedPosts())
                .thenReturn(List.of(postResponseDTO));

        mockMvc.perform(get("/post/posts/involved"))
                .andExpect(status().isOk());

        verify(postService).getInvolvedPosts();
    }

    @Test
    @DisplayName("Retrieve posts created by the user and return HTTP 200")
    void getOwnPosts_success() throws Exception {
        when(postService.getOwnPosts())
                .thenReturn(List.of(ownedPostResponseDTO));

        mockMvc.perform(get("/post/myposts"))
                .andExpect(status().isOk());

        verify(postService).getOwnPosts();
    }

    @Test
    @DisplayName("Get a post by its ID and return HTTP 200")
    void getPostById_success() throws Exception {
        when(postService.getPostById(1))
                .thenReturn(postResponseDTO);

        mockMvc.perform(get("/post/posts/1"))
                .andExpect(status().isOk());

        verify(postService).getPostById(1);
    }

    @Test
    @DisplayName("Add a comment to a post and return HTTP 200")
    void commentToPost_success() throws Exception {
        when(postService.commentToPost(any(), eq(1)))
                .thenReturn(postResponseDTO);

        mockMvc.perform(post("/post/posts/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCommentDTO)))
                .andExpect(status().isOk());

        verify(postService).commentToPost(any(PostCommentDTO.class), eq(1));
    }

    @Test
    @DisplayName("Apply to a post and return job application response")
    void applyToPost_success() throws Exception {
        when(postService.applyToPost(1))
                .thenReturn(jobApplicationResponseDTO);

        mockMvc.perform(post("/post/posts/1/apply"))
                .andExpect(status().isOk());

        verify(postService).applyToPost(1);
    }

    @Test
    @DisplayName("Check if the user has applied to a post and return true/false")
    void getAppliedStatus_success() throws Exception {
        when(postService.getAppliedStatus(1))
                .thenReturn(true);

        mockMvc.perform(get("/post/posts/1/applied-status"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(postService).getAppliedStatus(1);
    }

    @Test
    @DisplayName("Cancel a user's application to a post and return success message")
    void cancelApply_success() throws Exception {
        mockMvc.perform(post("/post/posts/1/cancelApply"))
                .andExpect(status().isOk())
                .andExpect(content().string("Jelentkezés sikeresen visszavonva!"));

        verify(postService).cancelApply(1);
    }

    @Test
    @DisplayName("Choose an applicant for a post and return updated post info")
    void chooseApplicant_success() throws Exception {
        when(postService.chooseApplicantForPost(1))
                .thenReturn(ownedPostResponseDTO);

        mockMvc.perform(post("/post/chooseApplicant/1"))
                .andExpect(status().isOk());

        verify(postService).chooseApplicantForPost(1);
    }

    @Test
    @DisplayName("Reject an applicant for a post and return success message")
    void rejectApply_success() throws Exception {
        mockMvc.perform(get("/post/posts/1/rejectApply"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "A munka sikeresen visszavonva! / A kiválasztott jelentkező sikeresen törölve!"
                ));

        verify(postService).rejectApply(1);
    }

    @Test
    @DisplayName("Change the status of a post and return updated status DTO")
    void changePostStatus_success() throws Exception {
        when(postService.changePostStatus(eq(1), eq("closed")))
                .thenReturn(changePostStatusDTO);

        mockMvc.perform(patch("/post/1/changeStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePostStatusDTO)))
                .andExpect(status().isOk());

        verify(postService).changePostStatus(1, "closed");
    }

    @Test
    @DisplayName("Close a post successfully and return confirmation message")
    void closePost_success() throws Exception {
        mockMvc.perform(post("/post/1/close"))
                .andExpect(status().isOk())
                .andExpect(content().string("A poszt sikeresen lezárva."));

        verify(postService).closePost(1, false);
    }

    @Test
    @DisplayName("Close an unsuccessful post and return confirmation message")
    void closeUnsuccessfulPost_success() throws Exception {
        mockMvc.perform(post("/post/1/unsuccessfulClose"))
                .andExpect(status().isOk())
                .andExpect(content().string("A poszt sikeresen lezárva."));

        verify(postService).closePost(1, true);
    }
}