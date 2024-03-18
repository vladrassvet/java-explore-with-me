package ru.practicum.ewm.repository.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.enums.CommentState;
import ru.practicum.ewm.model.comment.Comment;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Comment findCommentByAuthorAndEvent(User user, Event event);

    List<Comment> findAllByEventAndState(Event event, CommentState commentState);

    List<Comment> findAllByCreatedIsAfterAndCreatedIsBeforeOrderByCreated(LocalDateTime start,
                                                                          LocalDateTime end, Pageable pageable);

    boolean existsByAuthor(User user);
}