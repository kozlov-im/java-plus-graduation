package ru.practicum.service;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto add(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void delete(long userId);
}
