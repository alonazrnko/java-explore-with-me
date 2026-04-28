package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.RequestRepository;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceHelperTest {

    @Mock
    private EventMapper eventMapper;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private StatsClient statsClient;

    @InjectMocks
    private EventServiceHelper eventHelper;

    @Test
    void makeFullDto_shouldHandleStatsErrorGracefully() {
        Event event = new Event();
        event.setId(1L);
        when(statsClient.getStats(any(), any(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("Service down"));
        when(requestRepository.countByEventIdAndStatus(any(), any())).thenReturn(0);
        when(eventMapper.toEventFullDto(any(), anyInt(), anyLong())).thenReturn(new EventFullDto());

        EventFullDto result = eventHelper.makeFullDto(event);

        assertThat(result).isNotNull();
        verify(eventMapper).toEventFullDto(any(), anyInt(), eq(0L));
    }

    @Test
    void makeShortDtoList_shouldReturnEmpty_whenEventsEmpty() {
        List<EventShortDto> result = eventHelper.makeShortDtoList(Collections.emptyList());
        assertThat(result).hasSize(0);
        verifyNoInteractions(statsClient);
    }

    @Test
    void makeShortDtoList_shouldWorkWithData() {
        Event event = new Event();
        event.setId(1L);
        when(statsClient.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(new ViewStatsDto[]{ new ViewStatsDto("app", "/events/1", 10L) }));

        eventHelper.makeShortDtoList(List.of(event));

        verify(eventMapper).toEventShortDto(eq(event), anyInt(), eq(10L));
    }
}
