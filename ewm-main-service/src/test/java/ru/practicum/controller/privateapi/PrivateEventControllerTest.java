package ru.practicum.controller.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.*;
import ru.practicum.service.PrivateEventService;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrivateEventController.class)
class PrivateEventControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private PrivateEventService eventService;

    @Autowired
    private MockMvc mvc;

    @Test
    void createEvent_whenValid_thenReturns201() throws Exception {
        NewEventDto request = NewEventDto.builder()
                .annotation("Test annotation for the event")
                .category(1L)
                .description("Test description for the event")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new LocationDto(55.7541f, 37.6204f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Test Title")
                .build();

        EventFullDto response = EventFullDto.builder()
                .id(1L)
                .title("Test Title")
                .build();

        when(eventService.createEvent(anyLong(), any(NewEventDto.class))).thenReturn(response);

        mvc.perform(post("/users/{userId}/events", 1L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void getEvents_whenParamsOk_thenReturns200() throws Exception {
        when(eventService.getEvents(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(EventShortDto.builder().id(1L).build()));

        mvc.perform(get("/users/{userId}/events", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getEvents_whenInvalidPagination_thenReturns400() throws Exception {
        mvc.perform(get("/users/{userId}/events", 1L)
                        .param("size", "0")) // @Positive нарушен
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEventById_shouldReturnDto() throws Exception {
        when(eventService.getEventById(1L, 10L))
                .thenReturn(EventFullDto.builder().id(10L).build());

        mvc.perform(get("/users/{userId}/events/{eventId}", 1L, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void updateEvent_whenValid_thenReturnsOk() throws Exception {
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .title("Updated Title")
                .build();

        when(eventService.updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class)))
                .thenReturn(EventFullDto.builder().id(10L).title("Updated Title").build());

        mvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 10L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void getEventRequests_shouldReturnList() throws Exception {
        when(eventService.getEventRequests(1L, 10L))
                .thenReturn(List.of(ParticipationRequestDto.builder().id(1L).build()));

        mvc.perform(get("/users/{userId}/events/{eventId}/requests", 1L, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updateRequestStatus_shouldReturnResult() throws Exception {
        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();
        EventRequestStatusUpdateResult response = new EventRequestStatusUpdateResult();

        when(eventService.updateRequestStatus(anyLong(), anyLong(), any()))
                .thenReturn(response);

        mvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1L, 10L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
