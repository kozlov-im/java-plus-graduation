package ru.practicum.controller.pub;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventInitiatorDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.feignClient.EventServiceFeignClient;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController implements EventServiceFeignClient {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // Валидация диапазона дат
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала rangeStart не может быть позже даты окончания rangeEnd", "");
        }

        // Валидация параметров пагинации
        if (from < 0 || size <= 0) {
            throw new ValidationException("Параметры пагинации 'from' и 'size' должны быть >= 0 и > 0 соответственно", "");
        }

        String clientIp = request.getRemoteAddr();

        log.info("==> GET /events: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, clientIp);
    }

    @GetMapping("/{event-id}")
    public EventFullDto getEventById(@PathVariable("event-id") Long eventId, HttpServletRequest request) {
        log.info("Получение информации о событии с id={}", eventId);

        // Получение IP клиента
        String clientIp = request.getRemoteAddr();

        // Получение события через сервис
        return eventService.getEventById(eventId, clientIp);
    }

    @Override
    public EventInitiatorDto getEventWithInitiatorId(@RequestParam Long eventId) {
        log.info("Получение информации о событии с инициатором id={} ", eventId);
        return eventService.getEventWithInitiatorId(eventId);
    }

    @Override
    public void setConfirmedRequests(Long eventId, Integer count) {
        log.info("Устанавливаем подтвержденные запросы для события id={} ", eventId);
        eventService.setConfirmedRequests(eventId, count);
    }

    @GetMapping("/test")
    public String test(@RequestParam Long userId, Integer maxResults) {
        return eventService.getTest(userId, maxResults);
    }


}
