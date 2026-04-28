package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.model.enums.EventState;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.model.enums.StateAction;
import ru.practicum.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateEventServiceImplTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private EventMapper eventMapper;
    @Mock private RequestRepository requestRepository;
    @Mock private RequestMapper requestMapper;
    @Mock private EventServiceHelper eventHelper;

    @InjectMocks
    private PrivateEventServiceImpl privateService;

    @Test
    void createEvent_shouldThrowException_whenDateIsTooSoon() {
        NewEventDto request = new NewEventDto();
        request.setEventDate(LocalDateTime.now().plusHours(1));

        assertThatThrownBy(() -> privateService.createEvent(1L, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("at least 2 hours");
    }

    @Test
    void updateEvent_shouldThrowConflict_whenEventIsPublished() {
        Long userId = 1L;
        Long eventId = 1L;
        Event event = new Event();
        event.setState(EventState.PUBLISHED);

        when(eventRepository.findByIdAndInitiatorId(eventId, userId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> privateService.updateEvent(userId, eventId, new UpdateEventUserRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only pending or canceled events can be changed");
    }

    @Test
    void updateRequestStatus_shouldConfirmRequests_withinLimit() {
        Long userId = 1L;
        Long eventId = 1L;

        User initiator = new User();
        initiator.setId(userId);

        Event event = new Event();
        event.setInitiator(initiator);
        event.setParticipantLimit(2);

        ParticipationRequest req1 = ParticipationRequest.builder().id(1L).status(RequestStatus.PENDING).build();
        ParticipationRequest req2 = ParticipationRequest.builder().id(2L).status(RequestStatus.PENDING).build();

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findAllById(any())).thenReturn(List.of(req1, req2));
        when(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)).thenReturn(0);
        when(requestMapper.toDto(any())).thenReturn(new ParticipationRequestDto());

        EventRequestStatusUpdateResult result = privateService.updateRequestStatus(userId, eventId, updateRequest);

        assertThat(result.getConfirmedRequests()).hasSize(2);
        assertThat(req1.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
        assertThat(req2.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
        verify(requestRepository).saveAll(any());
    }

    @Test
    void updateRequestStatus_shouldRejectOthers_whenLimitIsReached() {
        Long userId = 1L;
        Long eventId = 1L;
        Event event = new Event();
        event.setInitiator(new User(userId, null, null));
        event.setParticipantLimit(1); // Лимит всего 1

        ParticipationRequest req1 = ParticipationRequest.builder().id(1L).status(RequestStatus.PENDING).build();
        ParticipationRequest req2 = ParticipationRequest.builder().id(2L).status(RequestStatus.PENDING).build();

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findAllById(any())).thenReturn(List.of(req1, req2));
        when(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)).thenReturn(0);
        when(requestMapper.toDto(any())).thenReturn(new ParticipationRequestDto());

        EventRequestStatusUpdateResult result = privateService.updateRequestStatus(userId, eventId, updateRequest);

        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests()).hasSize(1);
        assertThat(req1.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
        assertThat(req2.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    void updateEvent_shouldChangeStateToPending_whenSendToReview() {
        Long userId = 1L;
        Long eventId = 1L;
        Event event = new Event();
        event.setState(EventState.CANCELED);

        UpdateEventUserRequest request = new UpdateEventUserRequest();
        request.setStateAction(StateAction.SEND_TO_REVIEW);

        when(eventRepository.findByIdAndInitiatorId(eventId, userId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);

        privateService.updateEvent(userId, eventId, request);

        assertThat(event.getState()).isEqualTo(EventState.PENDING);
    }
}
