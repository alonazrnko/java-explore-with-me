package ru.practicum.controller.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.service.RequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrivateRequestController.class)
class PrivateRequestControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RequestService requestService;

    @Autowired
    private MockMvc mvc;

    @Test
    void createRequest_shouldReturn201() throws Exception {
        ParticipationRequestDto response = ParticipationRequestDto.builder()
                .id(1L)
                .event(10L)
                .requester(1L)
                .build();

        when(requestService.createRequest(anyLong(), anyLong())).thenReturn(response);

        mvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "10") // Передаем eventId как RequestParam
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.event").value(10));

        verify(requestService).createRequest(1L, 10L);
    }

    @Test
    void getUserRequests_shouldReturnList() throws Exception {
        when(requestService.getUserRequests(1L))
                .thenReturn(List.of(ParticipationRequestDto.builder().id(1L).build()));

        mvc.perform(get("/users/{userId}/requests", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(requestService).getUserRequests(1L);
    }

    @Test
    void cancelRequest_shouldReturnUpdatedDto() throws Exception {
        ParticipationRequestDto response = ParticipationRequestDto.builder()
                .id(5L)
                .status(RequestStatus.valueOf("CANCELED"))
                .build();

        when(requestService.cancelRequest(1L, 5L)).thenReturn(response);

        mvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(requestService).cancelRequest(1L, 5L);
    }
}
