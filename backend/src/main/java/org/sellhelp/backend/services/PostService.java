package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.CreatePostDTO;
import org.sellhelp.backend.dtos.requests.PostCommentDTO;
import org.sellhelp.backend.dtos.requests.UpdatePostDTO;
import org.sellhelp.backend.dtos.responses.*;
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
    private final JobApplicationRepository jobApplicationRepository;
    private final ModelMapper modelMapper;
    private final CurrentUser currentUser;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository,
                       ModelMapper modelMapper, CityRepository cityRepository,
                       CurrentUser currentUser, PostStatusRepository postStatusRepository,
                       CommentRepository commentRepository, JobApplicationRepository jobApplicationRepository){
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.postStatusRepository = postStatusRepository;
        this.commentRepository = commentRepository;
        this.jobApplicationRepository = jobApplicationRepository;
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

        PostStatus postStatus = postStatusRepository.findByStatusName("new").orElseThrow(
                () -> new EntityNotFoundException("A poszt státusz nem létezik!")
        );

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

    public List<PostResponseDTO> getAvailablePosts(){
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, PostResponseDTO.class))
                .filter(postResponseDTO -> Objects.equals(postResponseDTO.getStatusName(), "new"))
                .toList();
    }

    public List<PostResponseDTO> getInvolvedPosts(){
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, PostResponseDTO.class))
                .filter(postResponseDTO -> !Objects.equals(postResponseDTO.getStatusName(), "new"))
                .filter(postResponseDTO -> !postOwned(postResponseDTO.getId()))
                .toList();
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

    public JobApplicationResponseDTO applyToPost(Integer postId) {
        if(postOwned(postId)){
            throw new InvalidPermissionException("Saját poszthoz nem lehet jelentkezni!");
        }

        String publisherEmail = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(publisherEmail).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        if(post.getSelectedUser() != null){
            throw new RuntimeException("Már nem lehet jelentkezni a posztra!");
        }

        if (jobApplicationRepository.existsByApplicantAndJobPost(user, post)) {
            throw new IllegalStateException("Már jelentkeztél erre a posztra!");
        }

        JobApplication jobApplication = JobApplication.builder()
                .applicant(user)
                .jobPost(post)
                .build();

        post.getJobApplications().add(jobApplication);

        jobApplication.setJobPost(post);
        jobApplicationRepository.save(jobApplication);
        postRepository.save(post);

        return modelMapper.map(jobApplication, JobApplicationResponseDTO.class);
    }

    public void cancelApply(Integer postId){
        if(postOwned(postId)){
            throw new InvalidPermissionException("Saját posztnál nem lehet visszavonni a jelentkezést!");
        }

        String publisherEmail = currentUser.getCurrentlyLoggedUserEmail();

        User user = userRepository.findByEmail(publisherEmail).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        JobApplication jobApplication = jobApplicationRepository.findByApplicantAndJobPost(user, post).orElseThrow(
                () -> new EntityNotFoundException("Még nem adtál be jelentkezést az alábbi poszthoz!")
        );

        jobApplicationRepository.delete(jobApplication);
    }

    @Transactional
    public OwnedPostResponseDTO chooseApplicantForPost(Integer jobApplicationId){
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).orElseThrow(
                () -> new EntityNotFoundException("A jelentkezés nem létezik!")
        );

        Post post = postRepository.findById(jobApplication.getJobPost().getId()).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        if(!postOwned(post.getId())){
            throw new InvalidPermissionException("Nincs hozzáférésed a poszthoz!");
        }

        PostStatus postStatus = postStatusRepository.findByStatusName("accepted").orElseThrow(
                () -> new EntityNotFoundException("A poszt státusz nem létezik!")
        );

        User selectedUser = jobApplication.getApplicant();
        post.setSelectedUser(selectedUser);

        post.setPostStatus(postStatus);

        postRepository.save(post);

        return modelMapper.map(post, OwnedPostResponseDTO.class);
    }

    @Transactional
    public void rejectApply(Integer postId){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        if(post.getSelectedUser() == null){
            throw new InvalidPermissionException("Még nincs kiválasztva jelentkező a munkára!");
        }

        User user = userRepository.findByEmail(post.getSelectedUser().getEmail()).orElseThrow(
                () -> new EntityNotFoundException("A felhasználó nem található!")
        );

        JobApplication jobApplication = jobApplicationRepository.findByApplicantAndJobPost(user, post).orElseThrow(
                () -> new EntityNotFoundException("A jelentkezés nem létezik!")
        );

        String postStatusName = "";

        if(postOwned(postId)){
            postStatusName = "rejected_by_employer";
        }
        else{
            postStatusName = "withdrawn_by_employee";
        }

        PostStatus postStatus = postStatusRepository.findByStatusName(postStatusName).orElseThrow(
                () -> new EntityNotFoundException("A poszt státusz nem létezik!")
        );

        post.getJobApplications().remove(jobApplication);
        post.setPostStatus(postStatus);
        post.setSelectedUser(null);

        postRepository.save(post);
    }
}
