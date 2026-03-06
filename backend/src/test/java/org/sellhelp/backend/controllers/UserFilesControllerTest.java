package org.sellhelp.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.dtos.responses.ProfilePictureDTO;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTFilter;
import org.sellhelp.backend.services.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserFilesController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserFilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserFileService userFileService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private CurrentUser currentUser;

    @MockitoBean
    private JWTFilter jwtFilter;

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Get all files for the logged-in user successfully")
    void getAllFiles_success() throws Exception {
        when(userFileService.getAllUserFiles("test@test.com"))
                .thenReturn(List.of(new FileDTO(1, "http://url", "http://url2", "testFileName1")));

        mockMvc.perform(get("/user/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileId").value(1))
                .andExpect(jsonPath("$[0].downloadUrl").value("http://url"))
                .andExpect(jsonPath("$[0].openUrl").value("http://url2"))
                .andExpect(jsonPath("$[0].fileName").value("testFileName1"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Get a specific user file by fileId successfully")
    void getUserFile_success() throws Exception {
        when(userFileService.getUserFileByFileId(5))
                .thenReturn(new FileDTO(5, "http://download", "http://open", "MytestFileName22"));

        mockMvc.perform(get("/user/files/download/{fileId}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(5))
                .andExpect(jsonPath("$.downloadUrl").value("http://download"))
                .andExpect(jsonPath("$.openUrl").value("http://open"))
                .andExpect(jsonPath("$.fileName").value("MytestFileName22"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Get user file by id successfully (duplicate check)")
    void getUserFileById_success() throws Exception {
        when(userFileService.getUserFileByFileId(5))
                .thenReturn(new FileDTO(5, "http://download", "http://open", "new-testFileName333"));

        mockMvc.perform(get("/user/files/download/{fileId}", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(5))
                .andExpect(jsonPath("$.downloadUrl").value("http://download"))
                .andExpect(jsonPath("$.openUrl").value("http://open"))
                .andExpect(jsonPath("$.fileName").value("new-testFileName333"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Upload a file successfully for the logged-in user")
    void uploadFile_success() throws Exception {
        when(currentUser.getCurrentlyLoggedUserEmail())
                .thenReturn("test@test.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes());

        mockMvc.perform(multipart("/user/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Fájl sikeresen feltöltve."));

        verify(userFileService).addUserFile(eq("test@test.com"), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Delete a user file successfully")
    void deleteUserFile_success() throws Exception {
        mockMvc.perform(delete("/user/files/delete/{id}", 3))
                .andExpect(status().isOk())
                .andExpect(content().string("Fájl sikeresen törölve!"));

        verify(userFileService).deleteUserFile("test@test.com", 3);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Get logged-in user's own profile picture successfully")
    void getOwnProfilePicture_success() throws Exception {
        when(userFileService.getOwnProfilePicture("test@test.com"))
                .thenReturn(new ProfilePictureDTO("http://pp"));

        mockMvc.perform(get("/user/files/pp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").value("http://pp"));
    }

    @Test
    @WithMockUser
    @DisplayName("Get another user's profile picture successfully")
    void getOtherUsersProfilePicture_success() throws Exception {
        when(userFileService.getUserProfilePicture(7))
                .thenReturn(new ProfilePictureDTO("http://pp"));

        mockMvc.perform(get("/user/files/public/{id}/pp", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").value("http://pp"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Set profile picture successfully for logged-in user")
    void setProfilePicture_success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "file", "img.png",
                MediaType.IMAGE_PNG_VALUE,
                "image".getBytes());

        mockMvc.perform(multipart("/user/files/pp").file(image))
                .andExpect(status().isOk())
                .andExpect(content().string("Profilkép firssítve"));

        verify(userFileService).setProfilePicture(eq("test@test.com"), any());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Reject profile picture upload if file type is invalid")
    void setProfilePicture_invalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "nope".getBytes());

        mockMvc.perform(multipart("/user/files/pp").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("A fájl egy kép kell legyen!"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("Remove profile picture successfully for logged-in user")
    void removeProfilePicture_success() throws Exception {
        mockMvc.perform(delete("/user/files/pp"))
                .andExpect(status().isOk())
                .andExpect(content().string("Profilkép törölve!"));

        verify(userFileService).deleteProfilePicture("test@test.com");
    }
}