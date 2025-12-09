package org.example.authappbackened.services;

import org.example.authappbackened.dtos.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);

//    UserDto loginUser(UserDto userDto);
}
