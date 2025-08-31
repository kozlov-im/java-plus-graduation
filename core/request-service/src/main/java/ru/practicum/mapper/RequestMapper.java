package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.event.EventInitiatorDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.EventServiceFeignClient;
import ru.practicum.feignClient.UserServiceFeignClient;
import ru.practicum.model.Request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestMapper {

    UserServiceFeignClient userClient;
    EventServiceFeignClient eventClient;

    @Autowired
    public RequestMapper(EventServiceFeignClient eventClient, UserServiceFeignClient userClient) {
        this.eventClient = eventClient;
        this.userClient = userClient;
    }

    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEventId(),
                request.getRequester(),
                request.getStatus(),
                request.getCreated()
        );
    }

    public Request toRequest(ParticipationRequestDto participationRequestDto, Long requesterId, Long eventId) {
        Optional<List<UserDto>> optionalUserDto = Optional.ofNullable(userClient.getUsers(List.of(requesterId), 0, 1));
        List<UserDto> userDtos = optionalUserDto.filter(userDto -> !userDto.isEmpty()).orElseThrow(
                () -> new NotFoundException("User with id=" + requesterId + " not found!", ""));

        Optional<EventInitiatorDto> optionalEventInitiatorDto = Optional.ofNullable(eventClient.getEventWithInitiatorId(eventId));
        EventInitiatorDto eventInitiatorDto = optionalEventInitiatorDto.orElseThrow(() -> new NotFoundException("Event not found", ""));
        return new Request(
                null,
                eventInitiatorDto.getInitiatorId(),
                userDtos.getFirst().getId(),
                participationRequestDto.getStatus(),
                participationRequestDto.getCreated()
        );
    }

    public Request formUserAndEventToRequest(UserDto user, EventInitiatorDto event) {
        if (user == null || event == null) {
            return null;
        }

        Request request = new Request();
        request.setEventId(event.getId());
        request.setRequester(user.getId());
        request.setStatus(setStatus(event));
        request.setCreated(LocalDateTime.now());

        return request;
    }

    private RequestStatus setStatus(EventInitiatorDto event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return RequestStatus.CONFIRMED;
        }
        return RequestStatus.PENDING;
    }
}

