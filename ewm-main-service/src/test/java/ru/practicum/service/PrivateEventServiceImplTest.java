package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
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

    @Test
    void updateEvent_shouldApplyFullPatch() {
        Long userId = 1L;
        Long eventId = 10L;
        Event event = new Event();
        event.setState(EventState.PENDING);

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation("New Annotation updated")
                .category(2L)
                .description("New Description updated")
                .eventDate(LocalDateTime.now().plusDays(5))
                .location(new LocationDto(10.0f, 20.0f))
                .paid(true)
                .participantLimit(50)
                .requestModeration(false)
                .title("New Title")
                .stateAction(StateAction.SEND_TO_REVIEW)
                .build();

        when(eventRepository.findByIdAndInitiatorId(eventId, userId)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(new Category(2L, "New Cat")));
        when(locationRepository.save(any())).thenReturn(new Location(1L, 10.0f, 20.0f));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(eventHelper.makeFullDto(any())).thenReturn(new EventFullDto());

        privateService.updateEvent(userId, eventId, request);

        assertThat(event.getTitle()).isEqualTo("New Title");
        assertThat(event.getAnnotation()).isEqualTo("New Annotation updated");
        assertThat(event.getParticipantLimit()).isEqualTo(50);
        assertThat(event.getPaid()).isTrue();
        verify(eventRepository).save(event);
    }

    @Test
    void updateRequestStatus_shouldRejectAll_whenStatusIsRejected() {
        Long userId = 1L;
        Long eventId = 1L;
        Event event = new Event();
        event.setInitiator(new User(userId, null, null));

        ParticipationRequest req = ParticipationRequest.builder()
                .id(1L).status(RequestStatus.PENDING).build();

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus(RequestStatus.REJECTED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.findAllById(any())).thenReturn(List.of(req));
        when(requestMapper.toDto(any())).thenReturn(new ParticipationRequestDto());

        EventRequestStatusUpdateResult result = privateService.updateRequestStatus(userId, eventId, updateRequest);

        assertThat(result.getRejectedRequests()).hasSize(1);
        assertThat(req.getStatus()).isEqualTo(RequestStatus.REJECTED);
        verify(requestRepository).saveAll(any());
    }

    @Test
    void getEvents_shouldReturnList() {
        when(eventRepository.findAllByInitiatorId(anyLong(), any())).thenReturn(new PageImpl<>(List.of(new Event())));
        when(eventHelper.makeShortDtoList(any())).thenReturn(List.of(new EventShortDto()));

        List<EventShortDto> result = privateService.getEvents(1L, 0, 10);

        assertThat(result).hasSize(1);
    }
}
