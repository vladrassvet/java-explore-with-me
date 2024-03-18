package ru.practicum.ewm.service.comment;

import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.RequestCommentDto;
import ru.practicum.ewm.enums.CommentState;

import java.util.List;

public interface CommentService {

    CommentDto addComment(long userId, long eventId, RequestCommentDto requestCommentDto);

    CommentDto updateComment(long userId, long commentId, RequestCommentDto requestCommentDto);

    void deleteComment(long userId, long eventId);

    List<CommentDto> getCommentsByEven(long userId, long eventId);

    CommentDto updateStateComment(long commentId, CommentState commentState);

    List<CommentDto> getCommentsList(String rangeStart, String rangeEnd, int from, int size);
}