package ru.practicum.controller;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comment")
@AllArgsConstructor
@Validated
@Slf4j
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/event/{event-Id}")
    public List<CommentDto> getAllCommentsForEvent(@PathVariable("event-Id") @NotNull Long eventId,
                                                   @RequestParam(defaultValue = "0", required = false) Integer from,
                                                   @RequestParam(defaultValue = "10", required = false) Integer size) {

        log.info("Получение комментариев для события: eventId={}, from={}, size={}", eventId, from, size);
        return commentService.getAllCommentsForEvent(eventId, from, size);
    }
}
