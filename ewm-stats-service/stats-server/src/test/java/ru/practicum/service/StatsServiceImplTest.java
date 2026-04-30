package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private final LocalDateTime start = LocalDateTime.now().minusDays(1);
    private final LocalDateTime end = LocalDateTime.now();

    @Test
    void getStats_shouldThrowException_whenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now();

        assertThrows(IllegalArgumentException.class, () ->
                statsService.getStats(start, end, null, false)
        );
    }

    @Test
    void getStats_shouldCallUniqueStatsByUris_whenUniqueTrueAndUrisPresent() {
        statsService.getStats(LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of("/uri"), true);
        verify(statsRepository).getUniqueStatsByUris(any(), any(), anyList());
    }

    @Test
    void getStats_shouldCallStatsAllUris_whenUniqueFalseAndUrisEmpty() {
        statsService.getStats(LocalDateTime.now(), LocalDateTime.now().plusDays(1), Collections.emptyList(), false);
        verify(statsRepository).getStatsAllUris(any(), any());
    }

    @Test
    void getStats_whenStartAfterEnd_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                statsService.getStats(end, start, null, false));
    }

    @Test
    void getStats_whenUrisEmptyAndUniqueFalse_shouldCallStatsAllUris() {
        statsService.getStats(start, end, null, false);
        verify(statsRepository).getStatsAllUris(start, end);
    }

    @Test
    void getStats_whenUrisEmptyAndUniqueTrue_shouldCallUniqueStatsAllUris() {
        statsService.getStats(start, end, Collections.emptyList(), true);
        verify(statsRepository).getUniqueStatsAllUris(start, end);
    }

    @Test
    void getStats_whenUrisPresentAndUniqueFalse_shouldCallStatsByUris() {
        List<String> uris = List.of("/events/1");
        statsService.getStats(start, end, uris, false);
        verify(statsRepository).getStatsByUris(start, end, uris);
    }

    @Test
    void getStats_whenUrisPresentAndUniqueTrue_shouldCallUniqueStatsByUris() {
        List<String> uris = List.of("/events/1");
        statsService.getStats(start, end, uris, true);
        verify(statsRepository).getUniqueStatsByUris(start, end, uris);
    }

    @Test
    void saveHit_shouldSaveToRepository() {
        EndpointHitDto dto = new EndpointHitDto("app", "/uri", "192.168.0.1", LocalDateTime.now());

        statsService.saveHit(dto);

        verify(statsRepository).save(any(EndpointHit.class));
    }
}