package ru.practicum.ewm.service.comment;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.RequestCommentDto;
import ru.practicum.ewm.enums.CommentState;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.comment.CommentMapper;
import ru.practicum.ewm.model.comment.Comment;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.comment.CommentRepository;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public CommentDto addComment(long userId, long eventId, RequestCommentDto requestCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден или недоступен"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдена или недоступна"));
        if (commentRepository.existsByAuthor(user)) {
            throw new ConflictException("Нельзя оставить комментарий повторно");
        }
        Comment comment;
        try {
            comment = commentRepository.save(CommentMapper.toComment(user, event, requestCommentDto));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Нарушение целостности данных");
        }
        log.info("Добавлен новый комментарий {}", comment);
        return CommentMapper.toDto(comment);
    }

    @Override
    public CommentDto updateComment(long userId, long commentId, RequestCommentDto requestCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден или недоступен"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден или недоступен"));
        if (!user.equals(comment.getAuthor())) {
            throw new ValidationException("Данный пользователь не может внести изменения в комментарий");
        }
        comment.setComment(requestCommentDto.getComment());
        log.info("Изменения сохранены {}", comment);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден или недоступен"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдена или недоступна"));
        Comment comment = commentRepository.findCommentByAuthorAndEvent(user, event);
        log.info("Комментарий удален {}", comment);
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEven(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдена или недоступна"));
        List<Comment> commentList = commentRepository.findAllByEventAndState(event, CommentState.PUBLISHED);
        List<CommentDto> commentDtos = commentList.stream().map(CommentMapper::toDto).collect(Collectors.toList());
        log.info("Получен список комментариев {} к Event {} ", commentDtos, event);
        return commentDtos;
    }

    @Override
    public CommentDto updateStateComment(long commentId, CommentState commentState) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден или недоступен"));
        if (commentState == null) {
            throw new NotFoundException("Запрос составлен некорректно");
        }
        if (commentState.equals(CommentState.PUBLISHED)) {
            comment.setState(CommentState.PUBLISHED);
        }
        if (commentState.equals(CommentState.CANCELED)) {
            comment.setState(CommentState.CANCELED);
        }
        commentRepository.save(comment);
        log.info("Статус комментария поменян на {}", comment.getState());
        return CommentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsList(String rangeStart, String rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime start = LocalDateTime.parse(rangeStart, dateFormatter);
        LocalDateTime end = LocalDateTime.parse(rangeEnd, dateFormatter);
        if (start != null && end != null) {
            if (end.isBefore(start)) {
                throw new ValidationException("Запрос составлен не корректно");
            }
        }
        List<Comment> commentList = commentRepository.findAllByCreatedIsAfterAndCreatedIsBeforeOrderByCreated(start,
                end, pageable);
        List<CommentDto> commentDtos = commentList.stream().map(CommentMapper::toDto).collect(Collectors.toList());
        log.info("Получен список комментариев с заданными параметрами фильтрации {}", commentList);
        return commentDtos;
    }
}