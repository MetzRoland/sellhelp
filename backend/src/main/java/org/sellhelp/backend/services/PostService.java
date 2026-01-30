package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.CreatePostDTO;
import org.sellhelp.backend.entities.City;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.PostStatus;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.CityRepository;
import org.sellhelp.backend.repositories.PostRepository;
import org.sellhelp.backend.repositories.PostStatusRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final PostStatusRepository postStatusRepository;
    private final ModelMapper modelMapper;
    private final CurrentUser currentUser;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository,
                       ModelMapper modelMapper, CityRepository cityRepository,
                       CurrentUser currentUser, PostStatusRepository postStatusRepository){
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.postStatusRepository = postStatusRepository;
        this.modelMapper = modelMapper;
        this.currentUser = currentUser;
    }

    public Post createPost(CreatePostDTO createPostDTO){
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

        return post;
    }
}
