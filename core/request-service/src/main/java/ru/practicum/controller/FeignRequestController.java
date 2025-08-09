package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.enums.RequestStatus;
import ru.practicum.feignClient.RequestServiceFeignClient;
import ru.practicum.service.RequestService;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/requests")
public class FeignRequestController implements RequestServiceFeignClient {

    private final RequestService requestService;

    @Override
    public long getRequestsCountByStatusAndEventId(RequestStatus requestStatus, Long eventId) {
        log.info("getRequests for eventId {}", eventId);
        return requestService.getRequestsCountByStatusAndEventId(requestStatus, eventId);
    }
}
