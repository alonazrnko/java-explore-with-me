package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.RequestCount;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.enums.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("SELECT r.event.id AS eventId, COUNT(r.id) AS count " +
            "FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = :status " +
            "GROUP BY r.event.id")
    List<RequestCount> countByEventIdInAndStatus(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);
}
