package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.model.enums.AdminStateAction;
import ru.practicum.service.AdminEventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminEventController.class)
class AdminEventControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AdminEventService adminEventService;

    @Autowired
    private MockMvc mvc;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void getEvents_withAllParams_shouldReturnList() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        when(adminEventService.getEvents(anyList(), anyList(), anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(new EventFullDto()));

        mvc.perform(get("/admin/events")
                        .param("users", "1,2")
                        .param("states", "PUBLISHED,PENDING")
                        .param("categories", "1")
                        .param("rangeStart", start.format(formatter))
                        .param("rangeEnd", end.format(formatter))
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(adminEventService).getEvents(
                eq(List.of(1L, 2L)),
                anyList(),
                eq(List.of(1L)),
                any(),
                any(),
                eq(0),
                eq(10)
        );
    }

    @Test
    void getEvents_withDefaultParams_shouldReturnOk() throws Exception {
        when(adminEventService.getEvents(null, null, null, null, null, 0, 10))
                .thenReturn(List.of());

        mvc.perform(get("/admin/events"))
                .andExpect(status().isOk());
    }

    @Test
    void getEvents_whenInvalidPagination_shouldReturn400() throws Exception {
        mvc.perform(get("/admin/events")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_validRequest_shouldReturnDto() throws Exception {
        Long eventId = 1L;
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setTitle("New Super Event Title");
        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        EventFullDto response = new EventFullDto();
        response.setId(eventId);
        response.setTitle("New Super Event Title");

        when(adminEventService.updateEvent(eq(eventId), any(UpdateEventAdminRequest.class)))
                .thenReturn(response);

        mvc.perform(patch("/admin/events/{eventId}", eventId)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("New Super Event Title"));

        verify(adminEventService).updateEvent(eq(eventId), any());
    }

    @Test
    void updateEvent_whenTitleTooShort_shouldReturn400() throws Exception {
        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setTitle("Ab"); // Слишком короткое

        mvc.perform(patch("/admin/events/{eventId}", 1L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
