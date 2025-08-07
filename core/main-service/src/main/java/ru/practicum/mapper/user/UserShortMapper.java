package ru.practicum.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;


@Mapper(componentModel = "spring")
public interface UserShortMapper {

    UserShortDto toUserShortDto(UserDto user);

    @Mapping(target = "email", ignore = true)
    UserDto toUser(UserShortDto userShortDto);
}
