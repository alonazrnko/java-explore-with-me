package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService statsService;

    @Test
    void saveHit_shouldReturn201() throws Exception {
        EndpointHitDto dto = new EndpointHitDto("ewm-main-service", "/events/1", "192.163.0.1", LocalDateTime.now());

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(statsService).saveHit(any(EndpointHitDto.class));
    }

    @Test
    void getStats_shouldReturnList() throws Exception {
        when(statsService.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(List.of(new ViewStatsDto("app", "/uri", 10L)));

        mvc.perform(get("/stats")
                        .param("start", "2020-05-05 00:00:00")
                        .param("end", "2035-05-05 00:00:00")
                        .param("uris", "/events/1")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hits").value(10));
    }

    @Test
    void getStats_whenStartAfterEnd_shouldReturn400() throws Exception {
        when(statsService.getStats(any(), any(), any(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("Start after end"));

        mvc.perform(get("/stats")
                        .param("start", "2023-01-01 10:00:00")
                        .param("end", "2022-01-01 10:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveHit_whenInvalidDto_shouldReturn400() throws Exception {
        EndpointHitDto invalidDto = new EndpointHitDto();

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
