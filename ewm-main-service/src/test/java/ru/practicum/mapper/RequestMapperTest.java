package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.model.enums.RequestStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {

    private final RequestMapper mapper = new RequestMapper();

    @Test
    void shouldMapToDto_withFullData() {
        Event event = new Event();
        event.setId(5L);

        User requester = new User();
        requester.setId(10L);

        ParticipationRequest request = new ParticipationRequest();
        request.setId(1L);
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);

        ParticipationRequestDto dto = mapper.toDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEvent()).isEqualTo(5L);
        assertThat(dto.getRequester()).isEqualTo(10L);
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void shouldMapToDto_whenEventAndRequesterAreNull() {
        ParticipationRequest request = new ParticipationRequest();
        request.setId(1L);
        request.setStatus(RequestStatus.REJECTED);

        ParticipationRequestDto dto = mapper.toDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getEvent()).isNull();
        assertThat(dto.getRequester()).isNull();
    }

    @Test
    void shouldReturnNull_whenRequestIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
