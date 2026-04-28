package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.model.Category;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void shouldMapNewCategoryDtoToCategory() {
        NewCategoryDto request = new NewCategoryDto();
        request.setName("Concerts");

        Category category = mapper.toCategory(request);

        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("Concerts");
        assertThat(category.getId()).isNull();
    }

    @Test
    void shouldMapCategoryToCategoryDto() {
        Category category = new Category();
        category.setId(5L);
        category.setName("Exhibitions");

        CategoryDto dto = mapper.toCategoryDto(category);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("Exhibitions");
    }

    @Test
    void shouldReturnNull_whenNewCategoryDtoIsNull() {
        Category category = mapper.toCategory(null);

        assertThat(category).isNull();
    }

    @Test
    void shouldReturnNull_whenCategoryIsNull() {
        CategoryDto dto = mapper.toCategoryDto(null);

        assertThat(dto).isNull();
    }
}
