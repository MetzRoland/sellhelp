package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.CreatePostDTO;
import org.sellhelp.backend.dtos.requests.PostCommentDTO;
import org.sellhelp.backend.dtos.requests.UpdatePostDTO;
import org.sellhelp.backend.dtos.responses.OwnedPostResponseDTO;
import org.sellhelp.backend.dtos.responses.PostCommentResponseDTO;
import org.sellhelp.backend.dtos.responses.PostResponseInterface;
import org.sellhelp.backend.dtos.responses.PostResponseDTO;
import org.sellhelp.backend.entities.*;
import org.sellhelp.backend.exceptions.InvalidPermissionException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.*;
import org.sellhelp.backend.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final PostStatusRepository postStatusRepository;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;
    private final CurrentUser currentUser;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository,
                       ModelMapper modelMapper, CityRepository cityRepository,
                       CurrentUser currentUser, PostStatusRepository postStatusRepository,
                       CommentRepository commentRepository){
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.postStatusRepository = postStatusRepository;
        this.commentRepository = commentRepository;
        this.modelMapper = modelMapper;
        this.currentUser = currentUser;
    }

    public PostResponseDTO createPost(CreatePostDTO createPostDTO){
        String publisherEmail = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(publisherEmail).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        City city = cityRepository.findByCityName(createPostDTO.getCityName()).orElseThrow(
                () -> new EntityNotFoundException("A város nem található!")
        );

        PostStatus postStatus = postStatusRepository.findByStatusName("new");

        Post post = modelMapper.map(createPostDTO, Post.class);

        post.setPostPublisher(user);
        post.setCity(city);
        post.setPostStatus(postStatus);

        postRepository.save(post);

        return modelMapper.map(post, PostResponseDTO.class);
    }

    public OwnedPostResponseDTO updatePostData(UpdatePostDTO updatePostDTO, Integer postId){
        if(!postOwned(postId)) throw new InvalidPermissionException("Nincs hozzáférésed a poszthoz!");

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        if(updatePostDTO.getTitle() != null){
            post.setTitle(updatePostDTO.getTitle());
        }

        if(updatePostDTO.getDescription() != null){
            post.setDescription(updatePostDTO.getDescription());
        }

        if(updatePostDTO.getReward() != null){
            post.setReward(updatePostDTO.getReward());
        }

        if(updatePostDTO.getCityName() != null){
            City city = cityRepository.findByCityName(updatePostDTO.getCityName()).orElseThrow(
                    () -> new EntityNotFoundException("A város nem található!")
            );

            post.setCity(city);
        }

        postRepository.save(post);

        return modelMapper.map(post, OwnedPostResponseDTO.class);
    }

    public void deletePost(Integer postId){
        if(!postOwned(postId)) throw new InvalidPermissionException("Nincs hozzáférésed a poszthoz!");

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        postRepository.delete(post);
    }

    public boolean postOwned(Integer postId){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        String publisherEmail = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(publisherEmail).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        return Objects.equals(user.getId(), post.getPostPublisher().getId());
    }

    public List<PostResponseDTO> getAllPosts(){
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, PostResponseDTO.class)).toList();
    }

    public List<OwnedPostResponseDTO> getOwnPosts(){
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, OwnedPostResponseDTO.class))
                .filter(ownedPostResponseDTO -> postOwned(ownedPostResponseDTO.getId())).toList();
    }

    public PostResponseInterface getPostById(Integer postId){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem található!")
        );

        if (postOwned(postId)) {
            return modelMapper.map(post, OwnedPostResponseDTO.class);
        }
        else {
            return modelMapper.map(post, PostResponseDTO.class);
        }
    }

    public PostResponseInterface commentToPost(PostCommentDTO postCommentDTO, Integer postId){
        String publisherEmail = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(publisherEmail).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        Comment comment = modelMapper.map(postCommentDTO, Comment.class);

        comment.setCommentPublisher(user);
        comment.setPost(post);
        post.getPostComments().add(comment);
        postRepository.save(post);
        //commentRepository.save(comment);

        if(postOwned(postId)){
            OwnedPostResponseDTO postResponseDTO = modelMapper.map(post, OwnedPostResponseDTO.class);
            PostCommentResponseDTO postCommentResponseDTO = modelMapper.map(comment, PostCommentResponseDTO.class);
            postResponseDTO.getComments().add(postCommentResponseDTO);

            return postResponseDTO;
        }
        else{
            PostResponseDTO postResponseDTO = modelMapper.map(post, PostResponseDTO.class);
            PostCommentResponseDTO postCommentResponseDTO = modelMapper.map(comment, PostCommentResponseDTO.class);
            postResponseDTO.getComments().add(postCommentResponseDTO);

            return postResponseDTO;
        }
    }
}
