package ru.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldFindAllByInitiatorId() {
        User initiator = persistValidUser("init1@test.com");
        Category category = persistValidCategory("Concerts");
        Location location = persistValidLocation();

        Event event1 = persistValidEvent(initiator, category, location, "Event 1");
        Event event2 = persistValidEvent(initiator, category, location, "Event 2");

        User anotherUser = persistValidUser("other@test.com");
        persistValidEvent(anotherUser, category, location, "Event 3"); // Чужое событие

        Page<Event> result = eventRepository.findAllByInitiatorId(initiator.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Event::getTitle).containsExactlyInAnyOrder("Event 1", "Event 2");
    }

    @Test
    void shouldFindByIdAndInitiatorId() {
        User initiator = persistValidUser("init2@test.com");
        Category category = persistValidCategory("Exhibitions");
        Location location = persistValidLocation();
        Event event = persistValidEvent(initiator, category, location, "Target Event");

        Optional<Event> found = eventRepository.findByIdAndInitiatorId(event.getId(), initiator.getId());
        Optional<Event> notFoundWrongUser = eventRepository.findByIdAndInitiatorId(event.getId(), 999L);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Target Event");
        assertThat(notFoundWrongUser).isEmpty();
    }

    @Test
    void shouldCheckIfExistsByCategoryId() {
        User initiator = persistValidUser("init3@test.com");
        Category categoryWithEvent = persistValidCategory("Has Events");
        Category emptyCategory = persistValidCategory("Empty");
        Location location = persistValidLocation();

        persistValidEvent(initiator, categoryWithEvent, location, "Some Event");

        assertThat(eventRepository.existsByCategoryId(categoryWithEvent.getId())).isTrue();
        assertThat(eventRepository.existsByCategoryId(emptyCategory.getId())).isFalse();
    }


    private User persistValidUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return em.persistAndFlush(user);
    }

    private Category persistValidCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return em.persistAndFlush(category);
    }

    private Location persistValidLocation() {
        Location location = new Location();
        location.setLat(50.0f);
        location.setLon(50.0f);
        return em.persistAndFlush(location);
    }

    private Event persistValidEvent(User initiator, Category category, Location location, String title) {
        Event event = new Event();
        event.setTitle(title);
        event.setAnnotation("Short annotation for test");
        event.setDescription("Full description for test");
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(false);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        return em.persistAndFlush(event);
    }
}
