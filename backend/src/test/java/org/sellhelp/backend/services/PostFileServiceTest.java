package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.PostFile;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.exceptions.InvalidPermissionException;
import org.sellhelp.backend.repositories.PostFileRepository;
import org.sellhelp.backend.repositories.PostRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostFileServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private PostFileRepository postFileRepository;
    @Mock private S3Service s3Service;
    @Mock private CurrentUser currentUser;
    @Mock private MultipartFile multipartFile;

    @InjectMocks
    private PostFileService postFileService;

    private Post post;
    private PostFile postFile;
    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setId(1);

        post = new Post();
        post.setId(1);
        post.setPostPublisher(user);

        postFile = PostFile.builder()
                .id(1)
                .post(post)
                .postFilePath("key")
                .build();
    }

    // ------------------ getAllFilesForPost ------------------

    @Test
    void getAllFilesForPost_success() {
        FileDTO dto = new FileDTO();

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postFileRepository.findAllByPost(post)).thenReturn(List.of(postFile));
        when(s3Service.createFileDTO(1, "key")).thenReturn(dto);

        List<FileDTO> result = postFileService.getAllFilesForPost(1);

        assertEquals(1, result.size());
        verify(s3Service).createFileDTO(1, "key");
    }

    @Test
    void getAllFilesForPost_postNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> postFileService.getAllFilesForPost(1));
    }

    // ------------------ getPostFileById ------------------

    @Test
    void getPostFileById_success() {
        FileDTO dto = new FileDTO();

        when(postFileRepository.findById(1)).thenReturn(Optional.of(postFile));
        when(s3Service.createFileDTO(1, "key")).thenReturn(dto);

        FileDTO result = postFileService.getPostFileById(1);

        assertNotNull(result);
    }

    @Test
    void getPostFileById_notFound() {
        when(postFileRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> postFileService.getPostFileById(1));
    }

    // ------------------ addFileToPost ------------------

    @Test
    void addFileToPost_success() throws IOException {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(user);
        when(multipartFile.getOriginalFilename()).thenReturn("file.jpg");
        when(s3Service.postFileKey(1, "file.jpg")).thenReturn("key");
        when(postFileRepository.findByPostFilePath("key")).thenReturn(Optional.empty());
        when(postFileRepository.countByPost(post)).thenReturn(0);

        postFileService.addFileToPost(1, multipartFile);

        verify(s3Service).uploadFileWithKey("key", multipartFile);
        verify(postFileRepository).save(any(PostFile.class));
    }

    @Test
    void addFileToPost_postNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> postFileService.addFileToPost(1, multipartFile));
    }

    @Test
    void addFileToPost_notOwner() {
        User anotherUser = new User();
        anotherUser.setId(2);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(anotherUser);

        assertThrows(InvalidPermissionException.class,
                () -> postFileService.addFileToPost(1, multipartFile));
    }

    @Test
    void addFileToPost_duplicateFile() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(user);
        when(multipartFile.getOriginalFilename()).thenReturn("file.jpg");
        when(s3Service.postFileKey(1, "file.jpg")).thenReturn("key");
        when(postFileRepository.findByPostFilePath("key")).thenReturn(Optional.of(postFile));

        assertThrows(RuntimeException.class,
                () -> postFileService.addFileToPost(1, multipartFile));
    }

    @Test
    void addFileToPost_maxFilesExceeded() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(user);
        when(multipartFile.getOriginalFilename()).thenReturn("file.jpg");
        when(s3Service.postFileKey(1, "file.jpg")).thenReturn("key");
        when(postFileRepository.findByPostFilePath("key")).thenReturn(Optional.empty());
        when(postFileRepository.countByPost(post)).thenReturn(10);

        assertThrows(RuntimeException.class,
                () -> postFileService.addFileToPost(1, multipartFile));
    }

    @Test
    void addFileToPost_uploadFails() throws IOException {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(user);
        when(multipartFile.getOriginalFilename()).thenReturn("file.jpg");
        when(s3Service.postFileKey(1, "file.jpg")).thenReturn("key");
        when(postFileRepository.findByPostFilePath("key")).thenReturn(Optional.empty());
        when(postFileRepository.countByPost(post)).thenReturn(0);

        doThrow(IOException.class)
                .when(s3Service).uploadFileWithKey("key", multipartFile);

        assertThrows(RuntimeException.class,
                () -> postFileService.addFileToPost(1, multipartFile));
    }

    // ------------------ deletePostFile ------------------

    @Test
    void deletePostFile_success() {
        when(postFileRepository.findById(1)).thenReturn(Optional.of(postFile));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(user);

        postFileService.deletePostFile(1);

        verify(s3Service).deleteFile("key");
        verify(postFileRepository).delete(postFile);
    }

    @Test
    void deletePostFile_notFound() {
        when(postFileRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> postFileService.deletePostFile(1));
    }

    @Test
    void deletePostFile_notOwner() {
        User anotherUser = new User();
        anotherUser.setId(2);

        when(postFileRepository.findById(1)).thenReturn(Optional.of(postFile));
        when(currentUser.getCurrentlyLoggedUserEntity()).thenReturn(anotherUser);

        assertThrows(InvalidPermissionException.class,
                () -> postFileService.deletePostFile(1));
    }
}