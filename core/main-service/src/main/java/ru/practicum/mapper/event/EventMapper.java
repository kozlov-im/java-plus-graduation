package ru.practicum.mapper.event;

import org.mapstruct.Mapper;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;

import ru.practicum.mapper.location.LocationMapper;
import ru.practicum.mapper.user.UserShortMapper;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Mapper(componentModel = "spring", uses = {UserShortMapper.class, LocationMapper.class})
public interface EventMapper {

    Event toEvent(EventFullDto eventFullDto);

    EventShortDto toEventShortDto(Event event);

    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
