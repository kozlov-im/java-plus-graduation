package ru.practicum.mapper.request;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.UserServiceFeignClient;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestMapper {

    EventRepository eventRepository;
    UserServiceFeignClient userClient;

    @Autowired

    public RequestMapper(EventRepository eventRepository, UserServiceFeignClient userClient) {
        this.eventRepository = eventRepository;
        this.userClient = userClient;
    }

    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent().getId(),
                request.getRequester(),
                request.getStatus(),
                request.getCreated()
        );
    }

    public Request toRequest(ParticipationRequestDto participationRequestDto, Long requesterId, Long eventId) {
        Optional<List<UserDto>> optionalUserDto = Optional.ofNullable(userClient.getUsers(List.of(requesterId), 0, 1));
        List<UserDto> userDtos = optionalUserDto.filter(userDto -> !userDto.isEmpty()).orElseThrow(
                () -> new NotFoundException("User with id=" + requesterId + " not found!", ""));
        return new Request(
                null,
                eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found", "")),
                userDtos.getFirst().getId(),
                participationRequestDto.getStatus(),
                participationRequestDto.getCreated()
        );
    }

    public Request formUserAndEventToRequest(UserDto user, Event event) {
        if (user == null || event == null) {
            return null;
        }

        Request request = new Request();
        request.setEvent(event);
        request.setRequester(user.getId());
        request.setStatus(setStatus(event));
        request.setCreated(LocalDateTime.now());

        return request;
    }

    private RequestStatus setStatus(Event event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return RequestStatus.CONFIRMED;
        }
        return RequestStatus.PENDING;
    }
}
