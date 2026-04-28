package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent() != null ? request.getEvent().getId() : null)
                .requester(request.getRequester() != null ? request.getRequester().getId() : null)
                .status(request.getStatus())
                .build();
    }
}