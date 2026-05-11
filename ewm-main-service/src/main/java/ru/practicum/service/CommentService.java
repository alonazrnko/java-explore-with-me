package ru.practicum.service;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.model.enums.CommentState;

import java.util.List;

public interface CommentService {

    // Private API methods
    CommentDto addComment(Long userId, Long eventId, NewCommentDto commentDto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto);

    void deleteCommentByUser(Long userId, Long commentId);

    // Public API methods
    List<CommentDto> getEventComments(Long eventId, int from, int size);

    // Admin API methods
    CommentDto moderateComment(Long commentId, CommentState state);

    void deleteCommentByAdmin(Long commentId);
}
