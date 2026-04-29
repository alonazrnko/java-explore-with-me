package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CategoryServiceIntTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Test
    void updateCategory_whenNameConflicts_shouldCatchOrNotCatch() {
        Category cat1 = categoryRepository.save(Category.builder().name("Rock").build());
        categoryRepository.save(Category.builder().name("Jazz").build());
        CategoryDto request = new CategoryDto(cat1.getId(), "Jazz");

        assertThatThrownBy(() -> categoryService.updateCategory(cat1.getId(), request))
                .isInstanceOfAny(ConflictException.class, DataIntegrityViolationException.class);
    }

    @Test
    void updateCategory_whenNameConflicts_shouldThrowConflictException() {
        Category cat1 = categoryRepository.save(Category.builder().name("Rock").build());
        categoryRepository.save(Category.builder().name("Jazz").build());
        CategoryDto request = new CategoryDto(cat1.getId(), "Jazz");

        assertThatThrownBy(() -> categoryService.updateCategory(cat1.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void updateCategory_whenNameUnique_shouldSucceed() {
        Category cat1 = categoryRepository.save(Category.builder().name("Rock").build());
        CategoryDto request = new CategoryDto(cat1.getId(), "Blues");
        CategoryDto result = categoryService.updateCategory(cat1.getId(), request);

        assertThat(result.getName()).isEqualTo("Blues");
    }

    @Test
    void updateCategory_whenCalledFromOuterTransaction_flushHappensAtOuterCommit() {
        Category cat1 = categoryRepository.save(Category.builder().name("Rock").build());
        categoryRepository.save(Category.builder().name("Jazz").build());
        CategoryDto request = new CategoryDto(cat1.getId(), "Jazz");

        assertThatThrownBy(() -> callUpdateInNewTransaction(cat1.getId(), request))
                .isInstanceOfAny(ConflictException.class, DataIntegrityViolationException.class);
    }

    @Transactional
    public void callUpdateInNewTransaction(Long catId, CategoryDto request) {
        categoryService.updateCategory(catId, request);
    }
}
