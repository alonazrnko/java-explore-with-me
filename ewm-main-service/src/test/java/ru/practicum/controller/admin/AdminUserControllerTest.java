package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Test
    void createUser_whenValid_thenReturns201() throws Exception {
        NewUserRequest request = new NewUserRequest("user@example.com", "UserName");
        UserDto response = new UserDto(1L, "UserName", "user@example.com");

        when(userService.createUser(any(NewUserRequest.class))).thenReturn(response);

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UserName"))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(userService).createUser(any());
    }

    @Test
    void createUser_whenInvalidEmail_thenReturns400() throws Exception {
        NewUserRequest request = new NewUserRequest("wrong-email", "Name");

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    void getUsers_whenParamsOk_thenReturns200() throws Exception {
        when(userService.getUsers(any(), anyInt(), anyInt()))
                .thenReturn(List.of(new UserDto(1L, "Name", "email@test.com")));

        mvc.perform(get("/admin/users")
                        .param("ids", "1,2")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Name"));
    }

    @Test
    void getUsers_whenInvalidPagination_thenReturns400() throws Exception {
        mvc.perform(get("/admin/users")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
