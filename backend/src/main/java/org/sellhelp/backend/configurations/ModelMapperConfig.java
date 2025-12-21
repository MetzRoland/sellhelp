package org.sellhelp.backend.configurations;

import org.modelmapper.ModelMapper;
import org.sellhelp.backend.dtos.responses.SuperUserDTO;
import org.sellhelp.backend.dtos.responses.UserDTO;
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
                        src -> src.getUserSecret().isMfa(),
                        UserDTO::setMfa
                )
                .addMapping(
                        src -> src.getUserSecret().getTotpSecret(),
                        UserDTO::setTotpSecret
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
                );

        mapper.typeMap(User.class, SuperUserDTO.class)
                .addMapping(
                        src -> src.getUserSecret().isMfa(),
                        SuperUserDTO::setMfa
                )
                .addMapping(
                        src -> src.getRole().getRoleName(),
                        SuperUserDTO::setRole
                )
                .addMapping(
                        User::isBanned,
                        SuperUserDTO::setBanned
                );

        return mapper;
    }
}
