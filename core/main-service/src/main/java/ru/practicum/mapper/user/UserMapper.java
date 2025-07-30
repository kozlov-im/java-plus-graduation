package ru.practicum.mapper.user;

import org.mapstruct.Mapper;
import ru.practicum.dto.user.UserDto;
import ru.practicum.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toUserDto(User user);

    List<UserDto> toUserDtos(List<User> userList);
}
