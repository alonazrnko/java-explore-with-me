package ru.practicum.controller.publicapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCategoryController.class)
class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getCategories_whenParamsOk_thenReturns200() throws Exception {
        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("Concerts")
                .build();

        when(categoryService.getCategories(anyInt(), anyInt()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Concerts"));

        verify(categoryService).getCategories(0, 10);
    }

    @Test
    void getCategories_whenInvalidPagination_thenReturns400() throws Exception {
        mvc.perform(get("/categories")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCategoryById_shouldReturnDto() throws Exception {
        CategoryDto dto = CategoryDto.builder()
                .id(5L)
                .name("Theater")
                .build();

        when(categoryService.getCategoryById(5L)).thenReturn(dto);

        mvc.perform(get("/categories/{catId}", 5L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Theater"));

        verify(categoryService).getCategoryById(5L);
    }
}
