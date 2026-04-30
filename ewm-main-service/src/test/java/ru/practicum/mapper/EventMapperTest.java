package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.*;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventMapperTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private EventMapper eventMapper;

    @Test
    void shouldMapNewEventDtoToEventWithDefaultValues() {
        NewEventDto dto = new NewEventDto();
        dto.setTitle("Java Meetup");

        User initiator = new User();
        Category category = new Category();
        Location location = new Location();

        Event event = eventMapper.toEvent(dto, initiator, category, location);

        assertThat(event).isNotNull();
        assertThat(event.getTitle()).isEqualTo("Java Meetup");
        assertThat(event.getPaid()).isFalse();
        assertThat(event.getParticipantLimit()).isZero();
        assertThat(event.getRequestModeration()).isTrue();
        assertThat(event.getState()).isEqualTo(EventState.PENDING);
        assertThat(event.getCreatedOn()).isNotNull();
    }

    @Test
    void shouldMapEventToEventFullDto() {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Conference");
        event.setCategory(new Category());
        event.setLocation(new Location());
        event.setInitiator(new User());

        CategoryDto mockCategoryDto = new CategoryDto(1L, "IT");
        LocationDto mockLocationDto = new LocationDto(55.0f, 37.0f);

        when(categoryMapper.toCategoryDto(any())).thenReturn(mockCategoryDto);
        when(locationMapper.toLocationDto(any())).thenReturn(mockLocationDto);

        EventFullDto result = eventMapper.toEventFullDto(event, 10, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Conference");
        assertThat(result.getConfirmedRequests()).isEqualTo(10);
        assertThat(result.getViews()).isEqualTo(100L);
        assertThat(result.getCategory()).isEqualTo(mockCategoryDto);
        assertThat(result.getLocation()).isEqualTo(mockLocationDto);
    }

    @Test
    void shouldReturnNull_whenInputsAreNull() {
        assertThat(eventMapper.toEvent(null, new User(), new Category(), new Location())).isNull();
        assertThat(eventMapper.toEventFullDto(null, 0, 0L)).isNull();
        assertThat(eventMapper.toEventShortDto(null, 0, 0L)).isNull();
    }
}
