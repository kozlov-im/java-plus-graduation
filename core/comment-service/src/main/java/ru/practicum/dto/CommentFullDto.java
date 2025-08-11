package ru.practicum.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.event.EventInitiatorDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.model.Comment;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentFullDto {
    Long id;
    UserDto author;
    EventInitiatorDto event;
    String text;
    String created;
    String updated;
    Comment parentComment;
}
