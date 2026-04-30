package ru.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StatsRepositoryTest {

    @Autowired
    private StatsRepository statsRepository;

    @Test
    void getUniqueStatsByUris_shouldCountDistinctIp() {
        LocalDateTime now = LocalDateTime.now();
        statsRepository.save(new EndpointHit(null, "app", "/uri", "192.168.0.1", now));
        statsRepository.save(new EndpointHit(null, "app", "/uri", "192.168.0.1", now.plusMinutes(1)));

        List<ViewStatsDto> stats = statsRepository.getUniqueStatsByUris(
                now.minusHours(1), now.plusHours(1), List.of("/uri"));

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getHits()).isEqualTo(1L);
    }
}
