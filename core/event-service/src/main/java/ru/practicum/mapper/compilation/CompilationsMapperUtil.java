package ru.practicum.mapper.compilation;


import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.feignClient.UserServiceFeignClient;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.model.Compilation;

import java.util.ArrayList;
import java.util.List;

@Component
@Named("CompilationsMapperUtil")
@RequiredArgsConstructor
public class CompilationsMapperUtil {
    private final EventMapper eventMapper;
    private final UserServiceFeignClient userClient;

    @Named("getEventShortDtos")
    List<EventShortDto> getEventShortDtos(Compilation compilation) {
        if (compilation.getEvents().isEmpty()) {
            return new ArrayList<>();
        }
        return compilation.getEvents().stream().map(event -> eventMapper.toEventShortDto(event, userClient)).toList();
    }
}