package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentFullDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.UtilCommentClass;
import ru.practicum.model.Comment;
import ru.practicum.repository.CommentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UtilCommentClass utilCommentClass;

    @Override
    public List<CommentDto> getAllCommentsForEvent(Long eventId, int from, int size) {
        // Проверка корректности параметров
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("'from' должен быть >= 0, а 'size' > 0");
        }

        // Получение комментариев из репозитория
        List<Comment> comments = commentRepository.findAllByEventId(eventId, from, size);

        // Преобразование комментариев в DTO
        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public CommentFullDto createComment(NewCommentDto newCommentDto, Long eventId, Long userId) {
        CommentFullDto commentFullDto = utilCommentClass.toComment(newCommentDto, eventId, userId);
        Comment comment = utilCommentClass.fromCommentFullDto(commentFullDto);
        commentRepository.save(comment);
        return utilCommentClass.toCommentFullDto(comment);
    }

    @Override
    public CommentFullDto getComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment not found", "")
        );
        return utilCommentClass.toCommentFullDto(comment);
    }

    @Override
    public List<CommentDto> getAllCommentsForUser(Long userId, int from, int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("'from' должен быть >= 0, а 'size' > 0");
        }

        List<Comment> comments = commentRepository.findAllByUserId(userId, from, size);

        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment not found", "")
        );
        comment.setText(updateCommentDto.getText());
        commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment not found", "")
        );
        commentRepository.delete(comment);
    }

    @Override
    public CommentFullDto getCommentForAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment with id=" + commentId + " not found", ""));
        return utilCommentClass.toCommentFullDto(comment);
    }

    @Override
    public List<CommentFullDto> getAllUserCommentsForAdmin(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> allCommentsForUser = commentRepository.findAllByAuthorId(userId, pageable)
                .orElse(new ArrayList<>());
        return allCommentsForUser.stream().map(utilCommentClass::toCommentFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentFullDto> findAllCommentsByTextForAdmin(String text, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> allCommentsByText = commentRepository.findAllByText(text, pageable)
                .orElse(new ArrayList<>());
        return allCommentsByText.stream().map(utilCommentClass::toCommentFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment with id=" + commentId + " not found", ""));
        commentRepository.deleteById(commentId);

    }


}
