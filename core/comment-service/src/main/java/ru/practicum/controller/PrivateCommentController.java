package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentFullDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comment")
@AllArgsConstructor
@Validated
@Slf4j
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/event/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentFullDto createComment(@PathVariable @NotNull Long eventId,
                                        @RequestParam @NotNull Long userId,
                                        @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Private: post comment {}, userId {}, eventId {}", newCommentDto, userId, eventId);

        return commentService.createComment(newCommentDto, eventId, userId);
    }

    @GetMapping("/{commentId}")
    public CommentFullDto getComment(@PathVariable @NotNull Long commentId,
                                     @RequestParam @NotNull Long userId) {
        log.info("Private: get comment {}, userId {}", commentId, userId);
        return commentService.getComment(commentId, userId);
    }


    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable @NotNull Long commentId,
                                    @RequestParam @NotNull Long userId,
                                    @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("Private: patch comment {}, userId {}", updateCommentDto, userId);
        return commentService.updateComment(commentId, userId, updateCommentDto);
    }


    @GetMapping("/user/{userId}")
    public List<CommentDto> getAllCommentsForUser(@PathVariable @NotNull Long userId,
                                                  @RequestParam(defaultValue = "0", required = false) Integer from,
                                                  @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Private: get comments for user {}, from {}, size {}", userId, from, size);
        return commentService.getAllCommentsForUser(userId, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @NotNull Long commentId,
                              @RequestParam @NotNull Long userId) {
        log.info("Private: delete comment {}, userId {}", commentId, userId);
        commentService.deleteComment(commentId, userId);
    }
}
