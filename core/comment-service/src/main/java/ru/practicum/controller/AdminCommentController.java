package ru.practicum.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentFullDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comment")
@AllArgsConstructor
@Validated
@Slf4j
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentFullDto getCommentForAdmin(@PathVariable @NotNull Long commentId) {
        log.info("==> Comment with id={} for Admin was asked", commentId);
        return commentService.getCommentForAdmin(commentId);
    }

    @GetMapping("/user/{userId}")
    public List<CommentFullDto> getAllUserCommentsForAdmin(@PathVariable @NotNull Long userId,
                                                           @RequestParam(defaultValue = "0", required = false) Integer from,
                                                           @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("==> Comments for user with id={} from={} size={} for Admin was asked", userId, from, size);
        return commentService.getAllUserCommentsForAdmin(userId, from, size);
    }

    @GetMapping
    public List<CommentFullDto> findAllCommentsByTextForAdmin(@RequestParam @NotBlank String text,
                                                              @RequestParam(defaultValue = "0", required = false) Integer from,
                                                              @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("==> Comments with text={} from={} size={} for Admin was asked", text, from, size);
        return commentService.findAllCommentsByTextForAdmin(text, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable @NotNull Long commentId) {
        log.info("==> Comments with id={} was deleted by Admin", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}
