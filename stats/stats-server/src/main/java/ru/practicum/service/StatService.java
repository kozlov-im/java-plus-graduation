package ru.practicum.service;

import ru.practicum.dto.EndpointHitResponseDto;
import ru.practicum.dto.EndpointHitSaveRequestDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    EndpointHitResponseDto saveInfo(EndpointHitSaveRequestDto endpointHitSaveRequestDto);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

}
