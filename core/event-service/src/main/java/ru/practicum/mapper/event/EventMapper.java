package ru.practicum.mapper.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.dto.event.EventInitiatorDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;

import ru.practicum.feignClient.UserServiceFeignClient;
import ru.practicum.mapper.location.LocationMapper;
import ru.practicum.mapper.user.UserShortMapper;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Mapper(componentModel = "spring", uses = {UserShortMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(target = "initiatorId", source = "initiator", qualifiedByName = "userShortDtoToLong")
    Event toEvent(EventFullDto eventFullDto);

    @Mapping(target = "initiator", expression = "java(mapUserShortDto(event, userClient))")
    EventShortDto toEventShortDto(Event event, @Autowired UserServiceFeignClient userClient);

    EventInitiatorDto toEventInitiatorDto(Event event);

    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
    @Named("userShortDtoToLong")
    default Long userShortDtoToLong(UserShortDto userShortDto) {
        if (userShortDto == null) {
            return null;
        }
        return userShortDto.getId();
    }

    default UserShortDto mapUserShortDto(Event event, UserServiceFeignClient userClient) {
        UserDto userDto = userClient.getUsers(List.of(event.getInitiatorId()), 0, 1).getFirst();
        return new UserShortDto(userDto.getId(), userDto.getName());
    }


}
