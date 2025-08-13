package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.CommentFullDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.event.EventInitiatorDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.EventServiceFeignClient;
import ru.practicum.feignClient.UserServiceFeignClient;
import ru.practicum.model.Comment;
import ru.practicum.repository.CommentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UtilCommentClass {

    private final CommentRepository commentRepository;
    private final UserServiceFeignClient userClient;
    private final EventServiceFeignClient eventClient;

    public CommentFullDto toComment(NewCommentDto newCommentDto, Long eventId, Long userId) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());

        Optional<EventInitiatorDto> optionalEventInitiatorDto = Optional.ofNullable(eventClient.getEventWithInitiatorId(eventId));
        EventInitiatorDto eventInitiatorDto = optionalEventInitiatorDto.orElseThrow(() -> new NotFoundException("Event not found", ""));
        comment.setEventId(eventInitiatorDto.getId());

        Optional<List<UserDto>> optionalUserDto = Optional.ofNullable(userClient.getUsers(List.of(userId), 0, 1));
        optionalUserDto.filter(userDto -> !userDto.isEmpty()).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " not found!", ""));
        comment.setAuthorId(userId);

        if (newCommentDto.getParentComment() != null) {
            Comment parentComment = commentRepository.findById(newCommentDto.getParentComment())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found", ""));
            comment.setParent(parentComment);
        }

        LocalDateTime now = LocalDateTime.now();
        comment.setCreated(now);
        comment.setUpdated(now);

        Comment savedComment = commentRepository.save(comment);

        return toCommentFullDto(savedComment);
    }

    public CommentFullDto toCommentFullDto(Comment comment) {

        UserDto author = userClient.getUsers(List.of(comment.getAuthorId()), 0, 1).getFirst();
        EventInitiatorDto eventInitiatorDto = eventClient.getEventWithInitiatorId(comment.getEventId());

        CommentFullDto dto = new CommentFullDto();
        dto.setId(comment.getId());
        dto.setAuthor(author);
        dto.setEvent(eventInitiatorDto);
        dto.setText(comment.getText());
        dto.setCreated(comment.getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setUpdated(comment.getUpdated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // Обработка parentComment
        if (comment.getParent() != null) {
            dto.setParentComment(comment.getParent());
        }
        return dto;
    }

    public Comment fromCommentFullDto(CommentFullDto dto) {

        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setAuthorId(dto.getAuthor().getId());
        comment.setEventId(dto.getEvent().getId());
        comment.setText(dto.getText());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        comment.setCreated(LocalDateTime.parse(dto.getCreated(), formatter));
        comment.setUpdated(LocalDateTime.parse(dto.getUpdated(), formatter));

        if (dto.getParentComment() != null) {
            Comment parentComment = commentRepository.findById(dto.getParentComment().getId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found", ""));
            comment.setParent(parentComment);
        }

        return comment;
    }
}
