package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto dto) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(dto.getTimestamp());

        statsRepository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statsRepository.getUniqueStatsAllUris(start, end);
            } else {
                return statsRepository.getStatsAllUris(start, end);
            }
        } else {
            if (unique) {
                return statsRepository.getUniqueStatsByUris(start, end, uris);
            } else {
                return statsRepository.getStatsByUris(start, end, uris);
            }
        }
    }
}
