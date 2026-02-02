package org.sellhelp.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.dtos.responses.ProfilePictureDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserFile;
import org.sellhelp.backend.exceptions.InvalidPermissionException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.UserFileRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFileServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFileRepository userFileRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UserFileService userFileService;

    private User user;
    private UserFile userFile;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(1)
                .email("test@test.com")
                .build();

        userFile = UserFile.builder()
                .id(10)
                .user(user)
                .filePath("files/1/test.png")
                .build();
    }

    @Test
    void getAllUserFiles_success() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userFileRepository.findByUser(user))
                .thenReturn(List.of(userFile));
        when(s3Service.getDownloadURL(userFile.getFilePath()))
                .thenReturn("http://s3-url");

        List<FileDTO> result = userFileService.getAllUserFiles(user.getEmail());

        assertEquals(1, result.size());
        assertEquals(userFile.getId(), result.get(0).getFileId());
        assertEquals("http://s3-url", result.get(0).getUrl());
    }

    @Test
    void getAllUserFiles_userNotFound() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userFileService.getAllUserFiles("nope@test.com"));
    }

    @Test
    void getUserFile_success() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userFileRepository.findById(userFile.getId()))
                .thenReturn(Optional.of(userFile));
        when(s3Service.getDownloadURL(userFile.getFilePath()))
                .thenReturn("http://s3-url");

        FileDTO dto = userFileService.getUserFile(user.getEmail(), userFile.getId());

        assertEquals(userFile.getId(), dto.getFileId());
        assertEquals("http://s3-url", dto.getUrl());
    }

    @Test
    void deleteUserFile_invalidPermission() {
        User otherUser = User.builder().id(99).build();
        userFile.setUser(otherUser);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userFileRepository.findById(userFile.getId()))
                .thenReturn(Optional.of(userFile));

        assertThrows(InvalidPermissionException.class,
                () -> userFileService.deleteUserFile(user.getEmail(), userFile.getId()));
    }

    @Test
    void addUserFile_success() throws IOException {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(multipartFile.getOriginalFilename())
                .thenReturn("test.png");
        when(s3Service.fileKey(eq(user.getId()), any()))
                .thenReturn("files/1/test.png");

        userFileService.addUserFile(user.getEmail(), multipartFile);

        verify(userFileRepository).save(any(UserFile.class));
        verify(s3Service).uploadFileWithKey(anyString(), eq(multipartFile));
    }

    @Test
    void addUserFile_uploadFails() throws IOException {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(multipartFile.getOriginalFilename())
                .thenReturn("test.png");
        when(s3Service.fileKey(anyInt(), any()))
                .thenReturn("files/1/test.png");
        doThrow(IOException.class)
                .when(s3Service).uploadFileWithKey(anyString(), any());

        assertThrows(RuntimeException.class,
                () -> userFileService.addUserFile(user.getEmail(), multipartFile));
    }

    @Test
    void deleteUserFile_success() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userFileRepository.findById(userFile.getId()))
                .thenReturn(Optional.of(userFile));

        userFileService.deleteUserFile(user.getEmail(), userFile.getId());

        verify(s3Service).deleteFile(userFile.getFilePath());
        verify(userFileRepository).delete(userFile);
    }

    @Test
    void getProfilePicture_null() {
        user.setProfilePicturePath(null);

        ProfilePictureDTO dto =
                userFileService.getProfilePictureByUser(user);

        assertNull(dto.getProfilePictureUrl());
    }

    @Test
    void getProfilePicture_success() {
        user.setProfilePicturePath("pp/1.png");
        when(s3Service.getDownloadURL("pp/1.png"))
                .thenReturn("http://pp-url");

        ProfilePictureDTO dto =
                userFileService.getProfilePictureByUser(user);

        assertEquals("http://pp-url", dto.getProfilePictureUrl());
    }

    @Test
    void setProfilePicture_success() throws IOException {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(s3Service.ppKey(user.getId()))
                .thenReturn("pp/1.png");

        userFileService.setProfilePicture(user.getEmail(), multipartFile);

        verify(s3Service).uploadFileWithKey("pp/1.png", multipartFile);
        verify(userRepository).save(user);
        assertEquals("pp/1.png", user.getProfilePicturePath());
    }

    @Test
    void deleteProfilePicture_success() {
        user.setProfilePicturePath("pp/1.png");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(s3Service.ppKey(user.getId()))
                .thenReturn("pp/1.png");

        userFileService.deleteProfilePicture(user.getEmail());

        verify(s3Service).deleteFile("pp/1.png");
        verify(userRepository).save(user);
        assertNull(user.getProfilePicturePath());
    }
}
