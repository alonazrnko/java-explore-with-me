package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.model.Compilation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationMapperTest {

    private final CompilationMapper mapper = new CompilationMapper();

    @Test
    void shouldMapToDto() {
        Compilation compilation = new Compilation();
        compilation.setId(1L);
        compilation.setPinned(true);
        compilation.setTitle("Best Events");

        EventShortDto eventDto = new EventShortDto();
        eventDto.setId(10L);
        List<EventShortDto> events = List.of(eventDto);

        CompilationDto result = mapper.toDto(compilation, events);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPinned()).isTrue();
        assertThat(result.getTitle()).isEqualTo("Best Events");
        assertThat(result.getEvents()).hasSize(1);
    }

    @Test
    void shouldReturnNull_whenCompilationIsNull() {
        assertThat(mapper.toDto(null, List.of())).isNull();
    }
}
