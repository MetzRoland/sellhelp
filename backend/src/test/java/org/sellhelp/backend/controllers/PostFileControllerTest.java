package org.sellhelp.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.security.JWTFilter;
import org.sellhelp.backend.services.PostFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostFileController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PostFileService postFileService;

    private FileDTO fileDTO;

    @BeforeEach
    void init() {
        fileDTO = new FileDTO();
    }

    @Test
    void getAllPostFiles_success() throws Exception {
        when(postFileService.getAllFilesForPost(1))
                .thenReturn(List.of(fileDTO));

        mockMvc.perform(get("/post/files/all/1"))
                .andExpect(status().isOk());

        verify(postFileService).getAllFilesForPost(1);
    }

    @Test
    void getPostFile_success() throws Exception {
        when(postFileService.getPostFileById(1))
                .thenReturn(fileDTO);

        mockMvc.perform(get("/post/files/1/download"))
                .andExpect(status().isOk());

        verify(postFileService).getPostFileById(1);
    }

    @Test
    void addFileToPost_success() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy-content".getBytes()
        );

        mockMvc.perform(multipart("/post/files/upload/1")
                        .file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Fájl sikeresen feltöltve a poszthoz."));

        verify(postFileService).addFileToPost(eq(1), any());
    }

    @Test
    void deletePostFile_success() throws Exception {
        mockMvc.perform(delete("/post/files/1/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("Fájl sikeresen törölve"));

        verify(postFileService).deletePostFile(1);
    }
}