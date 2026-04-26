package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventState;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto request) {
        if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours in the future");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));

        Location location = Location.builder()
                .lat(request.getLocation().getLat())
                .lon(request.getLocation().getLon())
                .build();
        location = locationRepository.save(location);

        Event event = eventMapper.toEvent(request, user, category, location);
        event = eventRepository.save(event);

        return eventMapper.toEventFullDto(event, 0, 0L);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                // TODO: Replace 0 with actual requests/views logic via StatsClient and RequestRepository later
                .map(event -> eventMapper.toEventShortDto(event, 0, 0L))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // TODO: Replace 0 with actual requests/views logic later
        return eventMapper.toEventFullDto(event, 0, 0L);
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Event date must be at least 2 hours in the future");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getLocation() != null) {
            Location location = event.getLocation();
            location.setLat(request.getLocation().getLat());
            location.setLon(request.getLocation().getLon());
            locationRepository.save(location);
        }

        // TODO: Update views and confirmed requests when StatsClient is integrated
        return eventMapper.toEventFullDto(eventRepository.save(event), 0, 0L);
    }
}
