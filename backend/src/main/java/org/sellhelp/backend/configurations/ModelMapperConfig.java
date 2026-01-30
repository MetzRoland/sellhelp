package org.sellhelp.backend.configurations;

import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.requests.CreatePostDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
import org.sellhelp.backend.entities.Post;
import org.sellhelp.backend.entities.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.typeMap(User.class, UserDTO.class)
                .addMapping(
                        User::getId,
                        UserDTO::setId
                )
                .addMapping(
                        src -> src.getUserSecret().isMfa(),
                        UserDTO::setMfa
                )
                .addMapping(
                        src -> src.getCity().getCityName(),
                        UserDTO::setCityName
                )
                .addMapping(
                        User::isBanned,
                        UserDTO::setBanned
                )
                .addMapping(
                        src -> src.getRole().getRoleName(),
                        UserDTO::setRole
                )
                .addMapping(
                        User::getAuthProvider,
                        UserDTO::setAuthProvider
                );

        mapper.typeMap(Post.class, CreatePostDTO.class)
                .addMapping(
                        Post::getTitle,
                        CreatePostDTO::setTitle
                )
                .addMapping(
                        src -> src.getCity().getCityName(),
                        CreatePostDTO::setCityName
                )
                .addMapping(
                        Post::getDescription,
                        CreatePostDTO::setDescription
                )
                .addMapping(
                        Post::getReward,
                        CreatePostDTO::setReward
                );

        return mapper;
    }
}
