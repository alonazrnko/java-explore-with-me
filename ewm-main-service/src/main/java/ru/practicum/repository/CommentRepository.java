package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;
import ru.practicum.model.enums.CommentState;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventIdAndState(Long eventId, CommentState state, Pageable pageable);

    List<Comment> findAllByAuthorId(Long authorId, Pageable pageable);
}
