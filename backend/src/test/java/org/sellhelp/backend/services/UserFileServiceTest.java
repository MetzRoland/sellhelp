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
import org.sellhelp.backend.exceptions.WrongFileTypeException;
import org.sellhelp.backend.repositories.UserFileRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.FileTypeDetector;
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

    @Mock
    private FileTypeDetector fileTypeDetector;

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
        when(s3Service.createFileDTO(userFile.getId(), userFile.getFilePath()))
                .thenReturn(new FileDTO(userFile.getId(), "http://s3-url", "http://s3-url-open", "fileName"));

        List<FileDTO> result = userFileService.getAllUserFiles(user.getEmail());

        assertEquals(1, result.size());
        assertEquals(userFile.getId(), result.get(0).getFileId());
        assertEquals("http://s3-url", result.get(0).getDownloadUrl());
        assertEquals("http://s3-url-open", result.get(0).getOpenUrl());
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
        when(s3Service.createFileDTO(userFile.getId(), userFile.getFilePath()))
                .thenReturn(new FileDTO(userFile.getId(), "http://s3-url", "http://s3-url-open", "fileName"));

        FileDTO dto = userFileService.getUserFile(user.getEmail(), userFile.getId());

        assertEquals(userFile.getId(), dto.getFileId());
        assertEquals("http://s3-url", dto.getDownloadUrl());
        assertEquals("http://s3-url-open", dto.getOpenUrl());
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
        when(s3Service.userFileKey(eq(user.getId()), any()))
                .thenReturn("files/1/test.png");

        userFileService.addUserFile(user.getEmail(), multipartFile);

        verify(userFileRepository).save(any(UserFile.class));
        verify(s3Service).uploadFileWithKey(anyString(), eq(multipartFile));
    }

    @Test
    void addUserFile_successWithMultipleFiles() throws IOException {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userFileRepository.countByUser(user))
                .thenReturn(9);
        when(multipartFile.getOriginalFilename())
                .thenReturn("test.png");
        when(s3Service.userFileKey(eq(user.getId()), any()))
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
        when(s3Service.userFileKey(anyInt(), any()))
                .thenReturn("files/1/test.png");
        doThrow(IOException.class)
                .when(s3Service).uploadFileWithKey(anyString(), any());

        assertThrows(RuntimeException.class,
                () -> userFileService.addUserFile(user.getEmail(), multipartFile));
    }

    @Test
    void addUserFile_failTooManyFiles() throws IOException {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userFileRepository.countByUser(user))
                .thenReturn(10);

        assertThrows(RuntimeException.class,
                () -> userFileService.addUserFile(user.getEmail(), multipartFile));
    }

    @Test
    void addUserFile_failFileAlreadyExists() throws IOException {
        String key = "/users/55/filename.ext";
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(multipartFile.getOriginalFilename())
                .thenReturn("filename.ext");
        when(s3Service.userFileKey(user.getId(), "filename.ext"))
                .thenReturn(key);
        when(userFileRepository.findByFilePath(key))
                .thenReturn(Optional.of(new UserFile()));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userFileService.addUserFile(user.getEmail(), multipartFile));
        assertEquals("Ez a fájl már létezik", exception.getMessage());

        // verify upload was never called
        verify(s3Service, never()).uploadFileWithKey(anyString(), any());
        verify(userFileRepository, never()).save(any());
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
        when(fileTypeDetector.detectType(multipartFile))
                .thenReturn("image/png");
        when(s3Service.ppKey(user.getId()))
                .thenReturn("pp/1.png");

        userFileService.setProfilePicture(user.getEmail(), multipartFile);

        verify(s3Service).uploadFileWithKey("pp/1.png", multipartFile);
        verify(userRepository).save(user);
        assertEquals("pp/1.png", user.getProfilePicturePath());
    }

    @Test
    void setProfilePicture_fileTypeError() {
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(fileTypeDetector.detectType(multipartFile))
                .thenReturn("text/plain");

        assertThrows(WrongFileTypeException.class,
                () -> userFileService.setProfilePicture(user.getEmail(), multipartFile));
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
