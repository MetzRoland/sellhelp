package org.sellhelp.backend.controllers;

import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.services.PostFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/post/files")
public class PostFileController {
    private final PostFileService postFileService;

    public PostFileController(PostFileService postFileService)
    {
        this.postFileService = postFileService;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<FileDTO>> getAllPostFiles(@PathVariable int postId)
    {
        return ResponseEntity.ok(postFileService.getAllFilesForPost(postId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileDTO> getPostFile(@PathVariable int fileId)
    {
        return ResponseEntity.ok(postFileService.getPostFileById(fileId));
    }

    @PostMapping("/{postId}/upload")
    public ResponseEntity<String> addFileToPost(@PathVariable int postId, @RequestParam("file") MultipartFile file)
    {
        postFileService.addFileToPost(postId, file);
        return ResponseEntity.ok("Fájl sikeresen feltöltve a poszthoz.");
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deletePostFile(@PathVariable int fileId)
    {
        postFileService.deletePostFile(fileId);
        return ResponseEntity.ok("Fájl sikeresen törölve");
    }
}
