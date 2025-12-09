package org.example.authappbackened.configs;

import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.dtos.UserDto;
import org.example.authappbackened.entities.User;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        log.info("Configuring ModelMapper bean");
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(User.class, UserDto.class).addMappings(mapper -> {
            mapper.map(User::getName, UserDto::setUsername);
            mapper.map(User::getEmail, UserDto::setEmail);
        });
        return modelMapper;
    }
}
