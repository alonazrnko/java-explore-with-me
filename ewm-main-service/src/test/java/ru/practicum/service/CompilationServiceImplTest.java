package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock private CompilationRepository compilationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private CompilationMapper compilationMapper;
    @Mock private EventServiceHelper eventHelper;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    @Test
    void createCompilation_shouldSaveWithDefaultPinned_whenPinnedIsNull() {
        NewCompilationDto dto = new NewCompilationDto(null, null, "Summer Vibes");
        when(compilationRepository.save(any(Compilation.class))).thenAnswer(i -> i.getArguments()[0]);
        when(compilationMapper.toDto(any(), any())).thenReturn(new CompilationDto());

        compilationService.createCompilation(dto);

        verify(compilationRepository).save(argThat(c -> !c.getPinned() && c.getTitle().equals("Summer Vibes")));
    }

    @Test
    void createCompilation_shouldFetchEvents_whenEventIdsProvided() {
        NewCompilationDto dto = new NewCompilationDto(Set.of(1L), true, "Tech");
        when(eventRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(compilationRepository.save(any())).thenReturn(new Compilation());
        when(compilationMapper.toDto(any(), any())).thenReturn(new CompilationDto());

        compilationService.createCompilation(dto);

        verify(eventRepository).findAllById(Set.of(1L));
        verify(compilationRepository).save(any());
    }

    @Test
    void deleteCompilation_shouldThrowNotFound_whenIdInvalid() {
        when(compilationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> compilationService.deleteCompilation(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateCompilation_shouldPatchAllFields() {
        Long compId = 1L;
        Compilation compilation = Compilation.builder().id(compId).title("Old").pinned(false).build();
        UpdateCompilationRequest request = new UpdateCompilationRequest(Set.of(1L), true, "New Title");

        when(compilationRepository.findById(compId)).thenReturn(Optional.of(compilation));
        when(eventRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(compilationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(compilationMapper.toDto(any(), any())).thenReturn(new CompilationDto());

        compilationService.updateCompilation(compId, request);

        assertThat(compilation.getTitle()).isEqualTo("New Title");
        assertThat(compilation.getPinned()).isTrue();
        verify(compilationRepository).save(compilation);
    }

    @Test
    void getCompilations_shouldFilterByPinned() {
        when(compilationRepository.findAllByPinned(eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        compilationService.getCompilations(true, 0, 10);

        verify(compilationRepository).findAllByPinned(eq(true), any());
        verify(compilationRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getCompilationById_shouldReturnDto_whenFound() {
        Long compId = 1L;
        Compilation compilation = new Compilation();
        when(compilationRepository.findById(compId)).thenReturn(Optional.of(compilation));
        when(compilationMapper.toDto(any(), any())).thenReturn(new CompilationDto());

        compilationService.getCompilationById(compId);

        verify(eventHelper).makeShortDtoList(any());
        verify(compilationMapper).toDto(eq(compilation), any());
    }
}
