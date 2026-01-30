package org.sellhelp.backend.controllers;

import org.sellhelp.backend.dtos.requests.CreatePostDTO;
import org.sellhelp.backend.dtos.validationGroups.ValidationOrder;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService){
        this.postService = postService;
    }

    @PostMapping("/new")
    public Post createPost(@RequestBody @Validated(ValidationOrder.class) CreatePostDTO createPostDTO){
        return postService.createPost(createPostDTO);
    }
}
