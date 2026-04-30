package ru.practicum.controller.publicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.model.enums.EventSort;
import ru.practicum.service.PublicEventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicEventController.class)
class PublicEventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PublicEventService publicEventService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void getEvents_withAllParams_shouldReturnList() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        when(publicEventService.getEvents(anyString(), anyList(), anyBoolean(), any(), any(),
                anyBoolean(), any(EventSort.class), anyInt(), anyInt(), any(HttpServletRequest.class)))
                .thenReturn(List.of(EventShortDto.builder().id(1L).title("Public Event").build()));

        mvc.perform(get("/events")
                        .param("text", "search text")
                        .param("categories", "1,2")
                        .param("paid", "true")
                        .param("rangeStart", start.format(formatter))
                        .param("rangeEnd", end.format(formatter))
                        .param("onlyAvailable", "true")
                        .param("sort", "EVENT_DATE")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Public Event"));

        verify(publicEventService).getEvents(
                eq("search text"), eq(List.of(1L, 2L)), eq(true),
                any(), any(), eq(true), eq(EventSort.EVENT_DATE),
                eq(0), eq(10), any(HttpServletRequest.class)
        );
    }

    @Test
    void getEvents_withDefaultParams_shouldReturnOk() throws Exception {
        when(publicEventService.getEvents(
                any(),          // text
                any(),          // categories
                any(),          // paid
                any(),          // rangeStart
                any(),          // rangeEnd
                anyBoolean(),   // onlyAvailable
                any(),          // sort
                anyInt(),       // from
                anyInt(),       // size
                any(HttpServletRequest.class) // request
        )).thenReturn(List.of());

        mvc.perform(get("/events"))
                .andExpect(status().isOk());
    }

    @Test
    void getEvents_whenInvalidPagination_shouldReturn400() throws Exception {
        mvc.perform(get("/events")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEventById_shouldReturnDto() throws Exception {
        Long eventId = 1L;
        when(publicEventService.getEventById(eq(eventId), any(HttpServletRequest.class)))
                .thenReturn(EventFullDto.builder().id(eventId).title("Specific Event").build());

        mvc.perform(get("/events/{id}", eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Specific Event"));

        verify(publicEventService).getEventById(eq(eventId), any());
    }
}
