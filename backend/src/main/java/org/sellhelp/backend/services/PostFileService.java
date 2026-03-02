package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.PostFile;
import org.sellhelp.backend.exceptions.InvalidPermissionException;
import org.sellhelp.backend.repositories.PostFileRepository;
import org.sellhelp.backend.repositories.PostRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostFileService {
    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final S3Service s3Service;
    private final CurrentUser currentUser;

    public PostFileService(PostRepository postRepository, PostFileRepository postFileRepository,
                           S3Service s3Service, CurrentUser currentUser)
    {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
        this.s3Service = s3Service;
        this.currentUser = currentUser;
    }

    public List<FileDTO> getAllFilesForPost(int postId)
    {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("A poszt nem létezik!"));
        List<PostFile> postFiles = postFileRepository.findAllByPost(post);

        List<FileDTO> files = new ArrayList<>();
        for (PostFile f : postFiles) {
            FileDTO dto = s3Service.createFileDTO(f.getId(), f.getPostFilePath());
            files.add(dto);
        }

        return files;
    }

    public FileDTO getPostFileById(int postFileId)
    {
        PostFile postFile = postFileRepository.findById(postFileId)
                .orElseThrow(() -> new EntityNotFoundException("A fájl nem létezik!"));

        return s3Service.createFileDTO(postFile.getId(), postFile.getPostFilePath());
    }

    public void addFileToPost(int postId, MultipartFile file)
    {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("A poszt nem létezik!"));

        if (post.getPostPublisher().getId() != currentUser.getCurrentlyLoggedUserEntity().getId())
        {throw new InvalidPermissionException("Nincs jogosultság!");}

        String key = s3Service.postFileKey(post.getId(), file.getOriginalFilename());
        if (postFileRepository.findByPostFilePath(key).isPresent())
        {throw new RuntimeException("Ez a fájl már létezik");}

        if (postFileRepository.countByPost(post) >= 10)
        {
            throw new RuntimeException("Maximum 10 fájlt lehet feltölteni.");
        }

        PostFile newPostFile = PostFile.builder()
                .post(post)
                .postFilePath(key)
                .build();

        try {
            s3Service.uploadFileWithKey(newPostFile.getPostFilePath(), file);
            postFileRepository.save(newPostFile);
        } catch (IOException e) {
            throw new RuntimeException("A fájlt nem sikerült feltölteni!");
        }
    }

    public void deletePostFile(int postFileId)
    {
        PostFile postFile = postFileRepository.findById(postFileId)
                .orElseThrow(() -> new EntityNotFoundException("A fájl nem létezik!"));

        if (postFile.getPost().getPostPublisher().getId() != currentUser.getCurrentlyLoggedUserEntity().getId())
        {throw new InvalidPermissionException("Nincs jogosultság");}

        s3Service.deleteFile(postFile.getPostFilePath());
        postFileRepository.delete(postFile);
    }
}
