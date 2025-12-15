package org.sellhelp.backend.configurations;

import org.modelmapper.ModelMapper;
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
                );

        return mapper;
    }
}
