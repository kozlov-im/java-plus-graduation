package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.CommentDto;
import ru.practicum.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "author", source = "authorId")
    @Mapping(target = "text", source = "text")
    @Mapping(target = "created", source = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updated", source = "updated", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "replyComment", source = "parent", qualifiedByName = "toDtoRecursive")
    CommentDto toDto(Comment comment);

    @Named("toDtoRecursive")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "author", source = "authorId")
    @Mapping(target = "text", source = "text")
    @Mapping(target = "created", source = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updated", source = "updated", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "replyComment", ignore = true) // Предотвращение бесконечной рекурсии
    CommentDto toDtoRecursive(Comment comment);
}
