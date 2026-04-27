package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Event;
import ru.practicum.model.enums.EventSort;
import ru.practicum.model.enums.EventState;
import ru.practicum.repository.EventRepository;

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
    private final StatsClient statsClient;
    private final EventServiceHelper eventHelper;

    private static final String APP_NAME = "ewm-main-service";

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, EventSort sort, int from, int size,
                                         HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Start date must be before end date");
        }

        Pageable pageable = (sort == EventSort.EVENT_DATE)
                ? PageRequest.of(from / size, size, Sort.by("eventDate").ascending())
                : PageRequest.of(from / size, size);

        Specification<Event> spec = buildSpecification(text, categories, paid, rangeStart, rangeEnd);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        saveEndpointHit(request);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventShortDto> result = eventHelper.makeShortDtoList(events);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            Map<Long, Integer> limits = events.stream()
                    .collect(Collectors.toMap(Event::getId, Event::getParticipantLimit));

            result = result.stream()
                    .filter(dto -> {
                        Integer limit = limits.get(dto.getId());
                        return limit == 0 || dto.getConfirmedRequests() < limit;
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

        return eventHelper.makeFullDto(event);
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
}