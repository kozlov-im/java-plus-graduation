package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    UserDto add(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("==> add by newUserRequest = {}", newUserRequest);
        UserDto userDto = userService.add(newUserRequest);
        log.info("<== add result: {}", userDto);

        return userDto;
    }

    @GetMapping()
    List<UserDto> getUsers(@RequestParam(required = false, name = "ids") List<Long> ids,
                           @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                           @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero int size) {
        log.info("==> getUsers by ids = {}, from = {}, size = {}", ids, from, size);
        List<UserDto> userDtos = userService.getUsers(ids, from, size);
        log.info("<== getUsers result: {}", userDtos);
        return userDtos;
    }

    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable("user-id") @Positive long userId) {
        log.info("==> delete for userId = {}", userId);
        userService.delete(userId);
    }
}
