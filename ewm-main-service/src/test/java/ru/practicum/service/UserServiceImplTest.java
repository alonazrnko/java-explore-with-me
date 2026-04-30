package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUsers_shouldCallFindAll_whenIdsIsEmpty() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        userService.getUsers(null, 0, 10);

        verify(userRepository).findAll(any(Pageable.class));
        verify(userRepository, never()).findByIdIn(any(), any());
    }

    @Test
    void getUsers_shouldCallFindByIdIn_whenIdsIsProvided() {
        List<Long> ids = List.of(1L, 2L);
        when(userRepository.findByIdIn(eq(ids), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        userService.getUsers(ids, 0, 10);

        verify(userRepository).findByIdIn(eq(ids), any(Pageable.class));
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void deleteUser_shouldThrowNotFound_whenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void createUser_shouldReturnUserDto() {
        NewUserRequest request = new NewUserRequest("user@test.com", "Name");
        User user = new User(1L, "Name", "user@test.com");

        when(userMapper.toUser(request)).thenReturn(user);

        when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(new UserDto(1L, "Name", "user@test.com"));

        UserDto result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@test.com");

        verify(userRepository).saveAndFlush(any());
    }
}