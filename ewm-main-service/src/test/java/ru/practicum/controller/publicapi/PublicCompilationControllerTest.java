package ru.practicum.controller.publicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.CompilationDto;
import ru.practicum.service.CompilationService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCompilationController.class)
class PublicCompilationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CompilationService compilationService;

    @Test
    void getCompilations_withParams_shouldReturnList() throws Exception {
        CompilationDto response = CompilationDto.builder()
                .id(1L)
                .pinned(true)
                .title("Hot Summer Events")
                .events(Collections.emptyList())
                .build();

        when(compilationService.getCompilations(eq(true), anyInt(), anyInt()))
                .thenReturn(List.of(response));

        mvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Hot Summer Events"));

        verify(compilationService).getCompilations(true, 0, 10);
    }

    @Test
    void getCompilations_whenInvalidPagination_shouldReturn400() throws Exception {
        mvc.perform(get("/compilations")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCompilationById_shouldReturnDto() throws Exception {
        CompilationDto response = CompilationDto.builder()
                .id(10L)
                .title("Tech Meetups")
                .events(Collections.emptyList())
                .build();

        when(compilationService.getCompilationById(10L)).thenReturn(response);

        mvc.perform(get("/compilations/{compId}", 10L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Tech Meetups"));

        verify(compilationService).getCompilationById(10L);
    }
}
