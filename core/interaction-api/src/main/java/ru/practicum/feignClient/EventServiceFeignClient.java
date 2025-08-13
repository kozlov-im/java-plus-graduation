package ru.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventInitiatorDto;

@FeignClient(name = "event-service", path = "/events")
public interface EventServiceFeignClient {

    @PostMapping("/info")
    EventInitiatorDto getEventWithInitiatorId(@RequestParam Long eventId);

    @PostMapping("/confirm")
    void setConfirmedRequests(@RequestParam Long eventId, @RequestParam Integer count);

}
