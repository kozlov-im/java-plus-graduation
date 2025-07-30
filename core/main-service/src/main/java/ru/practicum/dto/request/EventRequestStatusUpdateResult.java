package ru.practicum.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateResult {
        List<ParticipationRequestDto> confirmedRequests;
        List<ParticipationRequestDto> rejectedRequests;
}
