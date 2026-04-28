package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventSpecificationsTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EventRepository eventRepository;

    private User initiator;
    private Category category;
    private Location location;

    @BeforeEach
    void setUp() {
        initiator = new User();
        initiator.setName("Owner");
        initiator.setEmail("owner@test.com");
        em.persist(initiator);

        category = new Category();
        category.setName("Tech");
        em.persist(category);

        location = new Location();
        location.setLat(10.0f);
        location.setLon(20.0f);
        em.persist(location);

        em.flush();
    }

    @Test
    void publicFilter_shouldSearchByTextIgnoringCase() {
        createEvent("Java for beginners", "Learn Java coding", EventState.PUBLISHED, LocalDateTime.now().plusDays(1));
        createEvent("Python Pro", "Advanced Python", EventState.PUBLISHED, LocalDateTime.now().plusDays(2));
        createEvent("C++ Basics", "Nothing about JaVa here", EventState.PUBLISHED, LocalDateTime.now().plusDays(3));

        Specification<Event> spec = EventSpecifications.publicFilter("jAvA", null, null, null, null);
        List<Event> result = eventRepository.findAll(spec);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Event::getTitle)
                .containsExactlyInAnyOrder("Java for beginners", "C++ Basics");
    }

    @Test
    void publicFilter_shouldReturnOnlyPublishedEvents() {
        createEvent("Published Event", "desc", EventState.PUBLISHED, LocalDateTime.now().plusDays(1));
        createEvent("Pending Event", "desc", EventState.PENDING, LocalDateTime.now().plusDays(1));
        createEvent("Canceled Event", "desc", EventState.CANCELED, LocalDateTime.now().plusDays(1));

        Specification<Event> spec = EventSpecifications.publicFilter(null, null, null, null, null);
        List<Event> result = eventRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Published Event");
    }

    @Test
    void publicFilter_shouldFilterByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        createEvent("Past Event", "desc", EventState.PUBLISHED, now.minusDays(1));
        createEvent("Target Event", "desc", EventState.PUBLISHED, now.plusDays(2));
        createEvent("Future Event", "desc", EventState.PUBLISHED, now.plusDays(10));

        Specification<Event> spec = EventSpecifications.publicFilter(null, null, null, now.plusDays(1), now.plusDays(5));
        List<Event> result = eventRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Target Event");
    }

    @Test
    void adminFilter_shouldFilterByMultipleStatesAndUsers() {
        User anotherUser = new User();
        anotherUser.setName("Other");
        anotherUser.setEmail("other@test.com");
        em.persist(anotherUser);

        createEvent("Event 1", "desc", EventState.PUBLISHED, initiator, category);
        createEvent("Event 2", "desc", EventState.PENDING, initiator, category);
        createEvent("Event 3", "desc", EventState.CANCELED, anotherUser, category);

        Specification<Event> spec = EventSpecifications.adminFilter(
                List.of(initiator.getId()),
                List.of(EventState.PUBLISHED, EventState.PENDING),
                null, null, null
        );
        List<Event> result = eventRepository.findAll(spec);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Event::getTitle)
                .containsExactlyInAnyOrder("Event 1", "Event 2");
    }

    private void createEvent(String title, String annotation, EventState state, LocalDateTime date) {
        createEvent(title, annotation, state, initiator, category, date);
    }

    private void createEvent(String title, String annotation, EventState state, User user, Category cat) {
        createEvent(title, annotation, state, user, cat, LocalDateTime.now().plusDays(1));
    }

    private void createEvent(String title, String annotation, EventState state, User user, Category cat, LocalDateTime date) {
        Event event = Event.builder()
                .title(title)
                .annotation(annotation)
                .description("Detailed description")
                .state(state)
                .initiator(user)
                .category(cat)
                .location(location)
                .eventDate(date)
                .createdOn(LocalDateTime.now())
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .build();
        em.persist(event);
        em.flush();
    }
}
