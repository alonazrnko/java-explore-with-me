package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.EndpointHit;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    // We will add custom JPQL queries here later to get statistics
    // for specific URIs and calculate unique IP hits.
}
