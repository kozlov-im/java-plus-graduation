package ru.practicum.service;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentFullDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    List<CommentDto> getAllCommentsForEvent(Long eventId, int from, int size);

    CommentFullDto createComment(NewCommentDto newCommentDto, Long eventId, Long userId);

    CommentFullDto getComment(Long commentId, Long userId);

    List<CommentDto> getAllCommentsForUser(Long userId, int from, int size);

    CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto updateCommentDto);

    void deleteComment(Long commentId, Long userId);

    CommentFullDto getCommentForAdmin(Long commentId);

    List<CommentFullDto> getAllUserCommentsForAdmin(Long userId, Integer from, Integer size);

    List<CommentFullDto> findAllCommentsByTextForAdmin(String text, Integer from, Integer size);

    void deleteCommentByAdmin(Long commentId);

}
