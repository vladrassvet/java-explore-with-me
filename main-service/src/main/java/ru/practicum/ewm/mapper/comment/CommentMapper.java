package ru.practicum.ewm.mapper.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.RequestCommentDto;
import ru.practicum.ewm.enums.CommentState;
import ru.practicum.ewm.model.comment.Comment;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment toComment(User user, Event event, RequestCommentDto requestCommentDto) {
        return Comment.builder()
                .comment(requestCommentDto.getComment())
                .event(event)
                .author(user)
                .created(LocalDateTime.now())
                .state(CommentState.PENDING)
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .author(comment.getAuthor().getName())
                .event(comment.getEvent().getAnnotation())
                .created(comment.getCreated())
                .state(comment.getState())
                .build();
    }
}