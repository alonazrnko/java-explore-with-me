package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.model.enums.CommentState;
import ru.practicum.service.CommentService;

@RestController
@RequestMapping("/admin/comments/{commentId}")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;

    @PatchMapping("/moderate")
    public CommentDto moderateComment(@PathVariable Long commentId,
                                      @RequestParam CommentState state) {
        log.info("Admin moderating comment id={} to state={}", commentId, state);
        return commentService.moderateComment(commentId, state);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Admin deleting comment id={}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}
