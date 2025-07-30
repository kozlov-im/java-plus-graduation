package ru.practicum.mapper.compilation;


import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.model.Compilation;

import java.util.ArrayList;
import java.util.List;

@Component
@Named("CompilationsMapperUtil")
@RequiredArgsConstructor
public class CompilationsMapperUtil {
    private final EventMapper eventMapper;

    @Named("getEventShortDtos")
    List<EventShortDto> getEventShortDtos(Compilation compilation) {
        if (compilation.getEvents().isEmpty()) {
            return new ArrayList<>();
        }
        return compilation.getEvents().stream().map(eventMapper::toEventShortDto).toList();
    }
}