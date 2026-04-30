package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void shouldMapNewUserRequestToUser() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Ivan");
        request.setEmail("ivan@test.ru");

        User user = mapper.toUser(request);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Ivan");
        assertThat(user.getEmail()).isEqualTo("ivan@test.ru");
        assertThat(user.getId()).isNull();
    }

    @Test
    void shouldMapUserToUserDto() {
        User user = new User();
        user.setId(10L);
        user.setName("Anna");
        user.setEmail("anna@test.ru");

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Anna");
        assertThat(dto.getEmail()).isEqualTo("anna@test.ru");
    }

    @Test
    void shouldReturnNull_whenRequestIsNull() {
        User user = mapper.toUser(null);

        assertThat(user).isNull();
    }

    @Test
    void shouldReturnNull_whenUserIsNull() {
        UserDto dto = mapper.toUserDto(null);

        assertThat(dto).isNull();
    }
}