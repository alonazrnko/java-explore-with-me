package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminCategoryController.class)
class AdminCategoryControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mvc;

    @Test
    void createCategory_whenValid_thenReturns201() throws Exception {
        NewCategoryDto request = NewCategoryDto.builder()
                .name("Concerts")
                .build();
        CategoryDto response = CategoryDto.builder()
                .id(1L)
                .name("Concerts")
                .build();

        when(categoryService.createCategory(any(NewCategoryDto.class))).thenReturn(response);

        mvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Concerts"));

        verify(categoryService).createCategory(any());
    }

    @Test
    void createCategory_whenNameIsBlank_thenReturns400() throws Exception {
        NewCategoryDto request = NewCategoryDto.builder()
                .name("")
                .build();

        mvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void updateCategory_whenValid_thenReturns200() throws Exception {
        Long catId = 1L;
        CategoryDto request = CategoryDto.builder()
                .name("Updated Festivals")
                .build();

        when(categoryService.updateCategory(eq(catId), any(CategoryDto.class))).thenReturn(request);

        mvc.perform(patch("/admin/categories/{catId}", catId)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Festivals"));

        verify(categoryService).updateCategory(eq(catId), any());
    }

    @Test
    void updateCategory_whenNameTooLong_thenReturns400() throws Exception {
        CategoryDto request = CategoryDto.builder()
                .name("A".repeat(51))
                .build();

        mvc.perform(patch("/admin/categories/{catId}", 1L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_shouldReturn204() throws Exception {
        mvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }
}
