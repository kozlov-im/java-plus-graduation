package ru.practicum.service.event;

import ru.practicum.dto.event.*;
import ru.practicum.enums.EventState;


import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getEventsForUser(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto eventDto);

    EventFullDto getEventByIdForUser(Long userId, Long eventId);

    EventFullDto changeEvent(Long userId, Long eventId, UpdateEventAdminRequest eventDto);

    List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Integer from, Integer size);

    EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);

    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                  Boolean onlyAvailable, String sort, int from, int size, String clientIp);

    EventFullDto getEventById(Long userId, Long eventId);

    EventInitiatorDto getEventWithInitiatorId(Long eventId);

    void setConfirmedRequests(Long eventId, Integer count);

    List<EventShortDto> getRecommendations(Long userId, Integer maxResults);

    void setLikeEvent(Long userId, Long eventId);

    void getInteractionsCount(List<Long> eventIds);
}

