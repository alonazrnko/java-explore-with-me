package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private EventRepository eventRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_shouldThrowConflict_whenNameExists() {
        NewCategoryDto dto = new NewCategoryDto("Rock");
        when(categoryMapper.toCategory(any())).thenReturn(new Category());
        when(categoryRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> categoryService.createCategory(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void updateCategory_shouldUpdateName() {
        Long catId = 1L;
        Category category = new Category(catId, "Old");
        CategoryDto request = new CategoryDto(catId, "New");

        when(categoryRepository.findById(catId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);
        when(categoryMapper.toCategoryDto(any())).thenReturn(request);

        CategoryDto result = categoryService.updateCategory(catId, request);

        assertThat(result.getName()).isEqualTo("New");
        verify(categoryRepository).save(category);
    }

    @Test
    void delete_shouldThrowConflict_whenEventsExist() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getCategories_shouldReturnList() {
        when(categoryRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Category())));
        when(categoryMapper.toCategoryDto(any())).thenReturn(new CategoryDto());

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertThat(result).hasSize(1);
        verify(categoryRepository).findAll(any(Pageable.class));
    }

    @Test
    void getCategoryById_shouldReturnDto() {
        Category category = new Category(1L, "Test");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toCategoryDto(any())).thenReturn(new CategoryDto(1L, "Test"));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertThat(result.getName()).isEqualTo("Test");
    }
}
