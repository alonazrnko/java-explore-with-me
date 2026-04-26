package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.LocationDto;
import ru.practicum.model.Location;

@Component
public class LocationMapper {

    public Location toLocation(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
