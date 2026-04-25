package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // По спецификации создание возвращает 201 Created
    public UserDto createUser(@Valid @RequestBody NewUserRequest request) {
        log.info("Admin request to create user: {}", request);
        return userService.createUser(request);
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                  @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Admin request to get users with ids={}, from={}, size={}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // По спецификации удаление возвращает 204 No Content
    public void deleteUser(@PathVariable Long userId) {
        log.info("Admin request to delete user with id={}", userId);
        userService.deleteUser(userId);
    }
}
