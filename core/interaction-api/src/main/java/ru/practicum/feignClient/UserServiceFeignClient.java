package ru.practicum.feignClient;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserServiceFeignClient {

    @GetMapping()
    List<UserDto> getUsers(@RequestParam(required = false, name = "ids") List<Long> ids,
                           @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                           @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero int size);
}
