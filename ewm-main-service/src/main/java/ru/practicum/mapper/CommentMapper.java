package ru.practicum.mapper;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.model.Comment;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(NewCommentDto newCommentDto) {
        if (newCommentDto == null) {
            return null;
        }
        return Comment.builder()
                .text(newCommentDto.getText())
                .build();
    }
}
