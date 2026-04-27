package ru.practicum.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.enums.EventSort;
import ru.practicum.model.enums.EventState;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    private static final String APP_NAME = "ewm-main-service";

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, EventSort sort, int from, int size,
                                         HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Start date must be before end date");
        }

        Pageable pageable;
        if (sort == EventSort.EVENT_DATE) {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        } else {
            pageable = PageRequest.of(from / size, size);
        }

        Specification<Event> spec = buildSpecification(text, categories, paid, rangeStart, rangeEnd);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        saveEndpointHit(request);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> viewsMap = getViewsForEvents(events);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    int confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return eventMapper.toEventShortDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(onlyAvailable)) {
            result = result.stream()
                    .filter(dto -> {
                        Event event = events.stream().filter(e -> e.getId().equals(dto.getId())).findFirst().orElseThrow();
                        return event.getParticipantLimit() == 0 || dto.getConfirmedRequests() < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
        }

        if (sort == EventSort.VIEWS) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return result;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " is not published");
        }

        saveEndpointHit(request);

        int confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        Map<Long, Long> viewsMap = getViewsForEvents(List.of(event));
        Long views = viewsMap.getOrDefault(eventId, 0L);

        return eventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    private Specification<Event> buildSpecification(String text, List<Long> categories, Boolean paid,
                                                    LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

            if (text != null && !text.isBlank()) {
                String likeText = "%" + text.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("annotation")), likeText),
                        cb.like(cb.lower(root.get("description")), likeText)
                ));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (paid != null) {
                predicates.add(cb.equal(root.get("paid"), paid));
            }
            if (rangeStart != null && rangeEnd != null) {
                predicates.add(cb.between(root.get("eventDate"), rangeStart, rangeEnd));
            } else {
                predicates.add(cb.greaterThan(root.get("eventDate"), LocalDateTime.now()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void saveEndpointHit(HttpServletRequest request) {
        try {
            EndpointHitDto hit = EndpointHitDto.builder()
                    .app(APP_NAME)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.saveHit(hit);
        } catch (Exception e) {
            log.error("Failed to save endpoint hit: {}", e.getMessage());
        }
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime start = LocalDateTime.now().minusYears(100);
        LocalDateTime end = LocalDateTime.now().plusYears(100);

        try {
            ResponseEntity<ViewStatsDto[]> response = statsClient.getStats(start, end, uris, true);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<Long, Long> viewsMap = new HashMap<>();
                for (ViewStatsDto stat : response.getBody()) {
                    Long eventId = Long.parseLong(stat.getUri().replace("/events/", ""));
                    viewsMap.put(eventId, stat.getHits());
                }
                return viewsMap;
            }
        } catch (Exception e) {
            log.error("Failed to fetch statistics for events: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }
}