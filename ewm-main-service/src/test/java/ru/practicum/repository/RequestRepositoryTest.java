package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.model.*;
import ru.practicum.model.enums.EventState;
import ru.practicum.model.enums.RequestStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RequestRepository requestRepository;

    private User requester;
    private Event event;

    @BeforeEach
    void setUp() {
        User initiator = new User(null, "Owner", "owner@test.com");
        em.persist(initiator);

        requester = new User(null, "Requester", "req@test.com");
        em.persist(requester);

        Category category = new Category(null, "Concerts");
        em.persist(category);

        Location location = new Location(null, 55.75f, 37.61f);
        em.persist(location);

        event = Event.builder()
                .title("Rock Concert")
                .annotation("Best rock event")
                .description("Detailed description")
                .initiator(initiator)
                .category(category)
                .location(location)
                .eventDate(LocalDateTime.now().plusDays(1))
                .state(EventState.PUBLISHED)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .createdOn(LocalDateTime.now())
                .build();
        em.persist(event);
        em.flush();
    }

    @Test
    void shouldCountConfirmedRequests() {
        createRequest(requester, event, RequestStatus.CONFIRMED);

        User user2 = new User(null, "User2", "user2@test.com");
        em.persist(user2);
        createRequest(user2, event, RequestStatus.CONFIRMED);

        createRequest(new User(null, "User3", "u3@test.com"), event, RequestStatus.PENDING); // Не должен учитываться

        Integer count = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCheckIfRequestExists() {
        createRequest(requester, event, RequestStatus.PENDING);

        assertThat(requestRepository.existsByRequesterIdAndEventId(requester.getId(), event.getId())).isTrue();
        assertThat(requestRepository.existsByRequesterIdAndEventId(999L, event.getId())).isFalse();
    }

    private void createRequest(User user, Event event, RequestStatus status) {
        if (user.getId() == null) em.persist(user);
        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .status(status)
                .created(LocalDateTime.now())
                .build();
        em.persist(request);
        em.flush();
    }
}
