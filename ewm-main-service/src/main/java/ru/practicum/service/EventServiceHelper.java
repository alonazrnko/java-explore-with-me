package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventServiceHelper {

    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    public List<EventShortDto> makeShortDtoList(Collection<Event> events) {
        if (events.isEmpty()) return Collections.emptyList();

        Map<Long, Long> views = getViews(events);
        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event,
                        requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public EventFullDto makeFullDto(Event event) {
        Map<Long, Long> views = getViews(List.of(event));
        int confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        return eventMapper.toEventFullDto(event, confirmedRequests, views.getOrDefault(event.getId(), 0L));
    }

    private Map<Long, Long> getViews(Collection<Event> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        try {
            var response = statsClient.getStats(LocalDateTime.now().minusYears(10),
                    LocalDateTime.now().plusYears(10), uris, true);
            if (response.getBody() != null) {
                return Arrays.stream(response.getBody())
                        .collect(Collectors.toMap(
                                s -> Long.parseLong(s.getUri().substring(s.getUri().lastIndexOf("/") + 1)),
                                ViewStatsDto::getHits
                        ));
            }
        } catch (Exception e) {
            log.error("Error fetching statistics from stats-service: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }

    public List<EventFullDto> makeFullDtoList(Collection<Event> events) {
        if (events.isEmpty()) return Collections.emptyList();

        Map<Long, Long> views = getViews(events);
        return events.stream()
                .map(event -> eventMapper.toEventFullDto(event,
                        requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }
}
