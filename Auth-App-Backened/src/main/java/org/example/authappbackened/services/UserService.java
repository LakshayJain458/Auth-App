package org.example.authappbackened.services;

import org.example.authappbackened.dtos.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    Iterable<UserDto> getAllUsers();
    UserDto getUserById(String id);
    UserDto getUserByEmail(String email);
    UserDto updateUser(String id, UserDto userDto);
    void deleteUser(String id);
}
