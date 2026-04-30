package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Event;
import ru.practicum.model.enums.AdminStateAction;
import ru.practicum.model.enums.EventState;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private EventServiceHelper eventHelper;

    @InjectMocks
    private AdminEventServiceImpl adminService;

    @Test
    void updateEvent_shouldPublishEvent_whenPending() {
        Long eventId = 1L;
        Event event = new Event();
        event.setId(eventId);
        event.setState(EventState.PENDING);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArguments()[0]);
        when(eventHelper.makeFullDto(any(Event.class))).thenReturn(new EventFullDto());

        adminService.updateEvent(eventId, request);

        assertThat(event.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(event.getPublishedOn()).isNotNull();
        verify(eventRepository).save(event);
    }

    @Test
    void updateEvent_shouldThrowConflict_whenPublishingNotPending() {
        Long eventId = 1L;
        Event event = new Event();
        event.setState(EventState.CANCELED); // Уже отменено

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> adminService.updateEvent(eventId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not in the PENDING state");

        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_shouldThrowValidationException_whenDateIsTooSoon() {
        Long eventId = 1L;
        Event event = new Event();

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setEventDate(LocalDateTime.now().plusMinutes(30));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> adminService.updateEvent(eventId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("at least 1 hour in the future");
    }

    @Test
    void updateEvent_shouldRejectEvent_whenNotPublished() {
        Long eventId = 1L;
        Event event = new Event();
        event.setState(EventState.PENDING);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.REJECT_EVENT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArguments()[0]);

        adminService.updateEvent(eventId, request);

        assertThat(event.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void updateEvent_shouldThrowConflict_whenRejectingPublished() {
        Long eventId = 1L;
        Event event = new Event();
        event.setState(EventState.PUBLISHED);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.REJECT_EVENT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> adminService.updateEvent(eventId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already published");
    }
}
