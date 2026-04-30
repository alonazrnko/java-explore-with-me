package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminCompilationController.class)
class AdminCompilationControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CompilationService compilationService;

    @Autowired
    private MockMvc mvc;

    @Test
    void createCompilation_whenValid_thenReturns201() throws Exception {
        NewCompilationDto request = NewCompilationDto.builder()
                .events(Set.of(1L, 2L))
                .pinned(true)
                .title("Summer Fest")
                .build();

        CompilationDto response = CompilationDto.builder()
                .id(1L)
                .pinned(true)
                .title("Summer Fest")
                .events(Collections.emptyList())
                .build();

        when(compilationService.createCompilation(any(NewCompilationDto.class))).thenReturn(response);

        mvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Summer Fest"))
                .andExpect(jsonPath("$.pinned").value(true));

        verify(compilationService).createCompilation(any());
    }

    @Test
    void createCompilation_whenTitleIsBlank_thenReturns400() throws Exception {
        NewCompilationDto request = new NewCompilationDto(null, true, "   ");

        mvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).createCompilation(any());
    }

    @Test
    void deleteCompilation_shouldReturn204() throws Exception {
        mvc.perform(delete("/admin/compilations/{compId}", 1L))
                .andExpect(status().isNoContent());

        verify(compilationService).deleteCompilation(1L);
    }

    @Test
    void updateCompilation_whenValid_thenReturns200() throws Exception {
        Long compId = 1L;
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(null)
                .pinned(false)
                .title("New Title")
                .build();

        CompilationDto response = CompilationDto.builder()
                .events(Collections.emptyList())
                .id(compId)
                .pinned(false)
                .title("New Title")
                .build();

        when(compilationService.updateCompilation(eq(compId), any(UpdateCompilationRequest.class)))
                .thenReturn(response);

        mvc.perform(patch("/admin/compilations/{compId}", compId)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.pinned").value(false));

        verify(compilationService).updateCompilation(eq(compId), any());
    }

    @Test
    void updateCompilation_whenTitleTooLong_thenReturns400() throws Exception {
        String longTitle = "A".repeat(51);
        UpdateCompilationRequest request = new UpdateCompilationRequest(null, null, longTitle);

        mvc.perform(patch("/admin/compilations/{compId}", 1L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
