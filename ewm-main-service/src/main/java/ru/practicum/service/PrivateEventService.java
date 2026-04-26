package ru.practicum.service;

import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;

import java.util.List;

public interface PrivateEventService {
    EventFullDto createEvent(Long userId, NewEventDto request);

    List<EventShortDto> getEvents(Long userId, int from, int size);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request);
}
