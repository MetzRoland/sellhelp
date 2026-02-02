package org.sellhelp.backend.controllers;

import org.sellhelp.backend.dtos.requests.ChangePostStatusDTO;
import org.sellhelp.backend.dtos.requests.CreatePostDTO;
import org.sellhelp.backend.dtos.requests.PostCommentDTO;
import org.sellhelp.backend.dtos.requests.UpdatePostDTO;
import org.sellhelp.backend.dtos.responses.JobApplicationResponseDTO;
import org.sellhelp.backend.dtos.responses.OwnedPostResponseDTO;
import org.sellhelp.backend.dtos.responses.PostResponseInterface;
import org.sellhelp.backend.dtos.responses.PostResponseDTO;
import org.sellhelp.backend.dtos.validationGroups.ValidationOrder;
import org.sellhelp.backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService){
        this.postService = postService;
    }

    @PostMapping("/new")
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody @Validated(ValidationOrder.class) CreatePostDTO createPostDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(createPostDTO));
    }

    @PatchMapping("/update/{postId}")
    public ResponseEntity<OwnedPostResponseDTO> updatePostData(@RequestBody @Validated(ValidationOrder.class) UpdatePostDTO updatePostDTO,
                                                          @PathVariable Integer postId){
        return ResponseEntity.ok(postService.updatePostData(updatePostDTO, postId));
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Integer postId){
        postService.deletePost(postId);

        return ResponseEntity.ok("A poszt törölve!");
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostResponseDTO>> getAvailablePosts(){
        return ResponseEntity.ok(postService.getAvailablePosts());
    }

    @GetMapping("/posts/involved")
    public ResponseEntity<List<PostResponseDTO>> getInvolvedPosts(){
        return ResponseEntity.ok(postService.getInvolvedPosts());
    }

    @GetMapping("/myposts")
    public ResponseEntity<List<OwnedPostResponseDTO>> getOwnPosts(){
        return ResponseEntity.ok(postService.getOwnPosts());
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponseInterface> getPostById(@PathVariable Integer postId){
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @PostMapping("/posts/{postId}/comment")
    public ResponseEntity<PostResponseInterface> commentToPost(@RequestBody @Validated(ValidationOrder.class) PostCommentDTO postCommentDTO,
                                                               @PathVariable Integer postId){
        return ResponseEntity.ok(postService.commentToPost(postCommentDTO, postId));
    }

    @PostMapping("/posts/{postId}/apply")
    public ResponseEntity<JobApplicationResponseDTO> applyToPost(@PathVariable Integer postId){
        return ResponseEntity.ok(postService.applyToPost(postId));
    }

    @PostMapping("/posts/{postId}/cancelApply")
    public ResponseEntity<String> cancelApply(@PathVariable Integer postId){
        postService.cancelApply(postId);

        return ResponseEntity.ok("Jelentkezés sikeresen visszavonva!");
    }

    @PostMapping("/chooseApplicant/{jobApplicationId}")
    public ResponseEntity<OwnedPostResponseDTO> chooseApplicantForPost(@PathVariable Integer jobApplicationId){
        return ResponseEntity.ok(postService.chooseApplicantForPost(jobApplicationId));
    }

    @GetMapping("/posts/{postId}/rejectApply")
    public ResponseEntity<String> rejectApply(@PathVariable Integer postId){
        postService.rejectApply(postId);

        return ResponseEntity.ok("A munka sikeresen visszavonva! / A kiválasztott jelentkező sikeresen törölve!");
    }

    @PatchMapping("/{postId}/changeStatus")
    public ResponseEntity<ChangePostStatusDTO> changePostStatus(@PathVariable Integer postId, @RequestBody ChangePostStatusDTO changePostStatusDTO) {
        return ResponseEntity.ok(postService.changePostStatus(postId, changePostStatusDTO.getTargetStatusName()));
    }
}
