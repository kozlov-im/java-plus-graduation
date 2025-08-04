package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotNull(message = "annotation required")
    @NotBlank(message = "annotation cannot be blank")
    @Length(min = 20, max = 2000)
    String annotation;
    @NotNull
    @Min(1L)
    Long category;
    @NotNull(message = "description required")
    @NotBlank(message = "description cannot be blank")
    @Length(min = 20, max = 7000)
    String description;
    @NotNull
    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    Location location;
    Boolean paid;
    @PositiveOrZero
    Integer participantLimit;
    Boolean requestModeration;
    @Length(min = 3, max = 120)
    String title;

    @AssertTrue(message = "Event date must be at least two hours from now")
    public boolean validateEventDate() {
        if (eventDate == null) {
            return true;
        }
        return eventDate.isAfter(LocalDateTime.now().plusHours(2));
    }

}
