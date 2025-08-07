package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.feignClient.UserServiceFeignClient;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@RequiredArgsConstructor
public class UserController implements UserServiceFeignClient {

    private final UserService userService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto add(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("==> add by newUserRequest = {}", newUserRequest);
        UserDto userDto = userService.add(newUserRequest);
        log.info("<== add result: {}", userDto);
        return userDto;
    }

    @GetMapping()
    public List<UserDto> getUsers(@RequestParam(required = false, name = "ids") List<Long> ids,
                           @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                           @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero int size) {
        log.info("==> getUsers by ids = {}, from = {}, size = {}", ids, from, size);
        List<UserDto> userDtos = userService.getUsers(ids, from, size);
        log.info("<== getUsers result: {}", userDtos);
        return userDtos;
    }

    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("user-id") @Positive long userId) {
        log.info("==> delete for userId = {}", userId);
        userService.delete(userId);
    }
}
