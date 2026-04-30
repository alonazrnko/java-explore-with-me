package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Event;
import ru.practicum.model.enums.EventState;
import ru.practicum.repository.EventRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private StatsClient statsClient;
    @Mock
    private EventServiceHelper eventHelper;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PublicEventServiceImpl publicService;

    @Test
    void getEvents_shouldThrowValidation_whenStartAfterEnd() {
        assertThatThrownBy(() -> publicService.getEvents(
                "text", null, null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                false, null, 0, 10, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getEventById_shouldSaveHitAndReturnDto() {
        Long eventId = 1L;
        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(request.getRequestURI()).thenReturn("/events/1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(eventHelper.makeFullDto(event)).thenReturn(new EventFullDto());

        publicService.getEventById(eventId, request);

        verify(statsClient, times(1)).saveHit(any());
        verify(eventRepository).findById(eventId);
    }

    @Test
    void getEventById_shouldThrowNotFound_whenEventNotPublished() {
        Long eventId = 1L;
        Event event = new Event();
        event.setState(EventState.PENDING);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> publicService.getEventById(eventId, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getEvents_shouldFilterOnlyAvailable() {
        Event event = new Event();
        event.setId(1L);
        event.setParticipantLimit(10);

        EventShortDto dto = new EventShortDto();
        dto.setId(1L);
        dto.setConfirmedRequests(10);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(eventHelper.makeShortDtoList(anyList())).thenReturn(List.of(dto));

        List<EventShortDto> result = publicService.getEvents(
                null, null, null, null, null,
                true, // onlyAvailable = true
                null, 0, 10, request);

        assertThat(result).isEmpty();
    }
}
