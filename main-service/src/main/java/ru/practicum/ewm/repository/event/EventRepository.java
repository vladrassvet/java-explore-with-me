package ru.practicum.ewm.repository.event;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    List<Event> findByIdIn(List<Long> id);

    List<Event> findAllByInitiatorOrderById(User initiator, PageRequest of);

    List<Event> findAllByCategory_Id(long catId);

}
