package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.constant.Constant;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {
    private final EventService eventService;
    private static final String PATH = "event-id";

    @GetMapping
    public List<EventFullDto> getEventsAdmin(@RequestParam(required = false) List<Long> users,
                                      @RequestParam(required = false) List<EventState> states,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = Constant.PATTERN_DATE)
                                      LocalDateTime rangeStart,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = Constant.PATTERN_DATE)
                                      LocalDateTime rangeEnd,
                                      @RequestParam(required = false, defaultValue = "0") Integer from,
                                      @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("==> Admin get events by: users ids={}, states={}, categories ids={}, rangeStart={}, rangeEnd={}," +
                "from={}, size={}", users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{event-id}")
    public EventFullDto updateEventByAdmin(@Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                                           @PathVariable(PATH) @NotNull @Min(1L) Long eventId) {
        log.info("==> Admin update event={} by id={}", updateEventAdminRequest, eventId);
        return eventService.updateEventByAdmin(updateEventAdminRequest, eventId);
    }
}
