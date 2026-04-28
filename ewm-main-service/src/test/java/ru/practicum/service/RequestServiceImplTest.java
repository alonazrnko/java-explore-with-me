package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventState;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock private RequestRepository requestRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private RequestMapper requestMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    void createRequest_shouldThrowConflict_whenUserIsInitiator() {
        Long userId = 1L;
        Long eventId = 10L;

        Event event = new Event();
        User initiator = new User();
        initiator.setId(userId);
        event.setInitiator(initiator);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.existsByRequesterIdAndEventId(userId, eventId)).thenReturn(false);

        assertThatThrownBy(() -> requestService.createRequest(userId, eventId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Initiator");
    }

    @Test
    void createRequest_shouldThrowConflict_whenEventNotPublished() {
        Long userId = 1L;
        Long eventId = 10L;
        Event event = new Event();
        event.setInitiator(new User(99L, null, null));
        event.setState(EventState.PENDING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> requestService.createRequest(userId, eventId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not published");
    }

    @Test
    void createRequest_shouldSetConfirmed_whenNoModerationAndNoLimit() {
        Long userId = 1L;
        Long eventId = 10L;
        Event event = Event.builder()
                .initiator(new User(99L, null, null))
                .state(EventState.PUBLISHED)
                .participantLimit(0)
                .requestModeration(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(requestMapper.toDto(any())).thenReturn(new ParticipationRequestDto());

        requestService.createRequest(userId, eventId);

        verify(requestRepository).save(argThat(req -> req.getStatus() == RequestStatus.CONFIRMED));
    }

    @Test
    void getUserRequests_shouldReturnList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequesterId(1L)).thenReturn(List.of(new ParticipationRequest()));
        when(requestMapper.toDto(any())).thenReturn(new ParticipationRequestDto());

        List<ParticipationRequestDto> result = requestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void createRequest_shouldThrowConflict_whenLimitReached() {
        User user = new User(1L, "U", "e");
        Event event = new Event();
        event.setInitiator(new User(2L, "O", "e"));
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(any(), any())).thenReturn(1);

        assertThatThrownBy(() -> requestService.createRequest(1L, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("limit reached");
    }

    @Test
    void cancelRequest_shouldSetStatusCanceled() {
        Long userId = 1L;
        Long reqId = 2L;
        User requester = new User(userId, "N", "E");
        ParticipationRequest request = ParticipationRequest.builder()
                .id(reqId).requester(requester).status(RequestStatus.PENDING).build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.findById(reqId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenReturn(request);

        requestService.cancelRequest(userId, reqId);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.CANCELED);
    }
}
