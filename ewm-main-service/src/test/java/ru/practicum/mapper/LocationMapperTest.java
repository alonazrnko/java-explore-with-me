package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.LocationDto;
import ru.practicum.model.Location;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private final LocationMapper mapper = new LocationMapper();

    @Test
    void shouldMapLocationToDto() {
        Location location = new Location(1L, 55.75f, 37.61f);
        LocationDto dto = mapper.toLocationDto(location);

        assertThat(dto).isNotNull();
        assertThat(dto.getLat()).isEqualTo(55.75f);
        assertThat(dto.getLon()).isEqualTo(37.61f);
    }

    @Test
    void shouldMapDtoToLocation() {
        LocationDto dto = new LocationDto(55.75f, 37.61f);
        Location location = mapper.toLocation(dto);

        assertThat(location).isNotNull();
        assertThat(location.getLat()).isEqualTo(55.75f);
        assertThat(location.getLon()).isEqualTo(37.61f);
        assertThat(location.getId()).isNull();
    }

    @Test
    void shouldReturnNull_whenInputsAreNull() {
        assertThat(mapper.toLocation(null)).isNull();
        assertThat(mapper.toLocationDto(null)).isNull();
    }
}
