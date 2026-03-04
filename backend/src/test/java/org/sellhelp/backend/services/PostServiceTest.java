package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.dtos.requests.CreatePostDTO;
import org.sellhelp.backend.dtos.requests.UpdatePostDTO;
import org.sellhelp.backend.dtos.responses.JobApplicationResponseDTO;
import org.sellhelp.backend.dtos.responses.OwnedPostResponseDTO;
import org.sellhelp.backend.dtos.responses.PostResponseDTO;
import org.sellhelp.backend.entities.*;
import org.sellhelp.backend.exceptions.InvalidPermissionException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.*;
import org.sellhelp.backend.security.CurrentUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private CityRepository cityRepository;
    @Mock private PostStatusRepository postStatusRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private JobApplicationRepository jobApplicationRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private CurrentUser currentUser;
    @Mock private EmailService emailService;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;
    private City city;
    private PostStatus postStatus;

    @BeforeEach
    void init() {
        user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        city = new City();
        city.setCityName("Budapest");

        postStatus = new PostStatus();
        postStatus.setStatusName("new");

        post = new Post();
        post.setId(1);
        post.setPostPublisher(user);
        post.setCity(city);
        post.setPostStatus(postStatus);
    }

    @Test
    void createPost_success() {
        CreatePostDTO dto = new CreatePostDTO();
        dto.setCityName("Budapest");

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cityRepository.findByCityName("Budapest")).thenReturn(Optional.of(city));
        when(postStatusRepository.findByStatusName("new")).thenReturn(Optional.of(postStatus));
        when(modelMapper.map(dto, Post.class)).thenReturn(post);
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(new PostResponseDTO());

        PostResponseDTO result = postService.createPost(dto);

        assertNotNull(result);
        verify(postRepository).save(post);
    }

    @Test
    void createPost_userNotFound() {
        CreatePostDTO dto = new CreatePostDTO();

        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> postService.createPost(dto));
    }

    @Test
    void updatePostData_success() {
        UpdatePostDTO dto = new UpdatePostDTO();
        dto.setTitle("Updated Title");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(post, OwnedPostResponseDTO.class)).thenReturn(new OwnedPostResponseDTO());

        OwnedPostResponseDTO response = postService.updatePostData(dto, 1);

        assertEquals("Updated Title", post.getTitle());
        verify(postRepository).save(post);
        assertNotNull(response);
    }

    @Test
    void updatePostData_notOwner() {
        User anotherUser = new User();
        anotherUser.setId(2);
        post.setPostPublisher(anotherUser);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidPermissionException.class,
                () -> postService.updatePostData(new UpdatePostDTO(), 1));
    }

    @Test
    void deletePost_success() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        postService.deletePost(1);

        verify(postRepository).delete(post);
    }

    @Test
    void getAvailablePosts_returnsOnlyNew() {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setStatusName("new");

        when(postRepository.findAll()).thenReturn(List.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(dto);

        List<PostResponseDTO> result = postService.getAvailablePosts();

        assertEquals(1, result.size());
    }

    @Test
    void applyToPost_success() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(user);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jobApplicationRepository.existsByApplicantAndJobPost(user, post)).thenReturn(false);
        when(modelMapper.map(any(JobApplication.class), eq(JobApplicationResponseDTO.class)))
                .thenReturn(new JobApplicationResponseDTO());

        var response = postService.applyToPost(1);

        assertNotNull(response);
        verify(jobApplicationRepository).save(any(JobApplication.class));
        verify(emailService).appliedToPost("test@example.com");
    }

    @Test
    void applyToPost_ownPost_throwsException() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidPermissionException.class,
                () -> postService.applyToPost(1));
    }

    @Test
    void closePost_success() {
        PostStatus closedStatus = new PostStatus();
        closedStatus.setStatusName("closed");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(postStatusRepository.findByStatusName("closed")).thenReturn(Optional.of(closedStatus));

        postService.closePost(1, false);

        assertEquals("closed", post.getPostStatus().getStatusName());
        verify(postRepository).save(post);
    }

    @Test
    void getAppliedStatus_true() {
        JobApplication jobApplication = new JobApplication();
        jobApplication.setApplicant(user);

        post.setJobApplications(List.of(jobApplication));

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(user);

        Boolean result = postService.getAppliedStatus(1);

        assertTrue(result);
    }
}