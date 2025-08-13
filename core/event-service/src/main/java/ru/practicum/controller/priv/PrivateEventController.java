package ru.practicum.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.event.EventService;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("users/{user-id}/events")
public class PrivateEventController {

    private static final String USERID = "user-id";
    private static final String EVENTID = "event-id";
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsForUser(@PathVariable(USERID) Long userId,
                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Private: get events userId {}, from {}, size {}", userId, from, size);
        return eventService.getEventsForUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable(USERID) Long userId,
                                    @RequestBody @Valid NewEventDto eventDto) {
        log.info("Private: post event {}, userId {}", eventDto, userId);
        return eventService.createEvent(userId, eventDto);
    }

    @GetMapping("{event-id}")
    public EventFullDto getEventByIdForUser(@PathVariable(USERID) Long userId, @PathVariable(EVENTID) Long eventId) {
        log.info("Private: get event userId {}, eventId {}", userId, eventId);
        return eventService.getEventByIdForUser(userId, eventId);
    }

    @PatchMapping("{event-id}")
    public EventFullDto changeEvent(@PathVariable(EVENTID) Long eventId, @PathVariable(USERID) Long userId,
                                    @RequestBody @Valid UpdateEventAdminRequest eventDto) {
        log.info("Private: patch user change event {}, userId {}, eventId {}", eventDto, userId, eventId);
        return eventService.changeEvent(userId, eventId, eventDto);
    }
}
