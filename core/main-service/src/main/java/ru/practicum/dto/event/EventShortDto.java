package ru.practicum.dto.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    String annotation;
    CategoryDto category;
    Integer confirmedRequests;
    String eventDate;
    Long id;
    UserShortDto initiator;
    Boolean paid;
    String title;
    Long views;



}
