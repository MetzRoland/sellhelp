package org.sellhelp.backend.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.ChangePostStatusDTO;
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
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final PostStatusRepository postStatusRepository;
    private final CommentRepository commentRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ModelMapper modelMapper;
    private final CurrentUser currentUser;
    private final EmailService emailService;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository,
                       ModelMapper modelMapper, CityRepository cityRepository,
                       CurrentUser currentUser, PostStatusRepository postStatusRepository,
                       CommentRepository commentRepository, JobApplicationRepository jobApplicationRepository,
                       EmailService emailService){
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.postStatusRepository = postStatusRepository;
        this.commentRepository = commentRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.modelMapper = modelMapper;
        this.currentUser = currentUser;
        this.emailService = emailService;
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

        if (isClosed(post)) {
            throw new IllegalStateException("A poszt már le van zárva");
        }

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

        log.info(publisherEmail);

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

    public List<PostResponseDTO> getInvolvedPosts() {
        Integer currentUserId = currentUser.getCurrentlyLoggedUserEntity().getId();

        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, OwnedPostResponseDTO.class))
                .filter(
                        ownedPostResponseDTO ->
                                (ownedPostResponseDTO.getSelectedUser() != null &&
                                        Objects.equals(ownedPostResponseDTO.getSelectedUser().getId(), currentUserId))
                                        ||
                                        ownedPostResponseDTO.getJobApplications().stream()
                                                .anyMatch(jobApplicationResponseDTO ->
                                                        Objects.equals(
                                                                jobApplicationResponseDTO.getApplicant().getId(),
                                                                currentUserId
                                                        )
                                                )
                )
                .map(ownedPostResponseDTO -> modelMapper.map(ownedPostResponseDTO, PostResponseDTO.class))
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

        if(currentUser.getCurrentlyLoggedUserEntity() == null){
            return modelMapper.map(post, PostResponseDTO.class);
        }

        if (postOwned(postId)) {
            log.info("tulajdonos");
            return modelMapper.map(post, OwnedPostResponseDTO.class);
        }
        else {
            log.info("nem tulajdonos");
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

        if (isClosed(post)) {
            throw new IllegalStateException("A poszt már le van zárva");
        }

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

        if (isClosed(post)) {
            throw new IllegalStateException("A poszt már le van zárva");
        }

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

        emailService.appliedToPost(publisherEmail);

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

        if (isClosed(post)) {
            throw new IllegalStateException("A poszt már le van zárva");
        }

        JobApplication jobApplication = jobApplicationRepository.findByApplicantAndJobPost(user, post).orElseThrow(
                () -> new EntityNotFoundException("Még nem adtál be jelentkezést az alábbi poszthoz!")
        );

        jobApplicationRepository.delete(jobApplication);

        emailService.cancelAppliedToPost(publisherEmail);
    }

    @Transactional
    public OwnedPostResponseDTO chooseApplicantForPost(Integer jobApplicationId){
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).orElseThrow(
                () -> new EntityNotFoundException("A jelentkezés nem létezik!")
        );

        Post post = postRepository.findById(jobApplication.getJobPost().getId()).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        if (isClosed(post)) {
            throw new IllegalStateException("A poszt már le van zárva");
        }

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

        emailService.gotSelectedToPost(selectedUser.getEmail());

        return modelMapper.map(post, OwnedPostResponseDTO.class);
    }

    @Transactional
    public void rejectApply(Integer postId){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        if (isClosed(post)) {
            throw new IllegalStateException("A poszt már le van zárva");
        }

        if(post.getSelectedUser() == null){
            throw new InvalidPermissionException("Még nincs kiválasztva jelentkező a munkára!");
        }

        User user = userRepository.findByEmail(post.getSelectedUser().getEmail()).orElseThrow(
                () -> new EntityNotFoundException("A felhasználó nem található!")
        );

        JobApplication jobApplication = jobApplicationRepository.findByApplicantAndJobPost(user, post).orElseThrow(
                () -> new EntityNotFoundException("A jelentkezés nem létezik!")
        );

        PostStatus postStatus = postStatusRepository.findByStatusName("new").orElseThrow(
                () -> new EntityNotFoundException("A poszt státusz nem létezik!")
        );

        post.getJobApplications().remove(jobApplication);
        post.setPostStatus(postStatus);
        post.setSelectedUser(null);

        postRepository.save(post);

        if(postOwned(postId)){
            emailService.gotRejectedToPost(user.getEmail());
        }
        else{
            emailService.cancelSelectedToPost(post.getPostPublisher().getEmail());
        }
    }

    public ChangePostStatusDTO changePostStatus(Integer postId, String targetStatusName) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("A poszt nem létezik!"));

        if(!postOwned(postId) && !Objects.equals(post.getSelectedUser().getId(), currentUser.getCurrentlyLoggedUserEntity().getId())){
            throw new InvalidPermissionException("Nem te vagy a munkavállaló, nincs jogusultságot!");
        }

        if (isClosed(post)) {
            throw new IllegalStateException("A poszt már le van zárva");
        }

        String currentStatus = post.getPostStatus().getStatusName();
        boolean isEmployer = postOwned(postId);
        boolean isEmployee = !isEmployer;

        switch (currentStatus) {

            case "accepted" -> {
                if (!isEmployee || !"started".equals(targetStatusName)) {
                    throw new IllegalStateException("Az alkalmazott csak 'started' státuszra válthat.");
                }
            }

            case "started" -> {
                if (!isEmployee || !"completed_by_employee".equals(targetStatusName)) {
                    throw new IllegalStateException("Az alkalmazott csak 'completed_by_employee' státuszra válthat.");
                }
            }

            case "completed_by_employee" -> {
                if (!isEmployer ||
                        (!"work_rejected".equals(targetStatusName) && !"closed".equals(targetStatusName))) {
                    throw new IllegalStateException("A munkáltató csak 'work_rejected' vagy 'closed' státuszt választhat.");
                }
            }

            case "work_rejected" -> {
                if (!isEmployer ||
                        (!"started".equals(targetStatusName) && !"unsuccessful_result_closed".equals(targetStatusName))) {
                    throw new IllegalStateException("A munkáltató csak 'started' vagy 'unsuccessful_result_closed' státuszt választhat.");
                }
            }

            default -> throw new IllegalStateException("A státusz nem módosítható ebből az állapotból.");
        }

        PostStatus newStatus = postStatusRepository.findByStatusName(targetStatusName)
                .orElseThrow(() -> new EntityNotFoundException("A poszt státusz nem létezik!"));

        post.setPostStatus(newStatus);
        postRepository.save(post);

        return new ChangePostStatusDTO(targetStatusName);
    }

    public void closePost(Integer postId, boolean isUnsuccessful) {
        if(!postOwned(postId)){
            throw new InvalidPermissionException("Csak a munkáltató zárhatja le a posztot!");
        }

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        if (isClosed(post))
        {throw new IllegalStateException("A poszt már le van zárva");}

        if (post.getPostStatus().getStatusName().equals("closed"))
        {
            throw new InvalidPermissionException("A poszt már le van zárva.");
        }

        PostStatus closedStatus;
        if (isUnsuccessful) {
            closedStatus = postStatusRepository.findByStatusName("unsuccessful_result_closed")
                    .orElseThrow(() -> new EntityNotFoundException("A poszt státusz nem létezik!"));
        }
        else {
            closedStatus = postStatusRepository.findByStatusName("closed")
                    .orElseThrow(() -> new EntityNotFoundException("A poszt státusz nem létezik!"));
        }

        post.setPostStatus(closedStatus);
        postRepository.save(post);
    }

    public boolean isClosed(Post post)
    {
        return post.getPostStatus().getStatusName().equals("closed") ||
                post.getPostStatus().getStatusName().equals("unsuccessful_result_closed");
    }

    public Boolean getAppliedStatus(Integer postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new EntityNotFoundException("A poszt nem létezik!")
        );

        User user = currentUser.getCurrentlyLoggedUserEntity();

        for(JobApplication jobApplication: post.getJobApplications()){
            if(Objects.equals(jobApplication.getApplicant().getId(), user.getId())){
                return true;
            }
        }

        return false;
    }
}
