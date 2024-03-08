package ru.practicum.ewm.repository.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.enums.ParticipationState;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.request.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByEvent(Event event);

    List<ParticipationRequest> findAllByEventIn(List<Event> eventListResult);

    List<ParticipationRequest> findAllByEventAndStatusOrderByCreated(Event event, ParticipationState confirmed);

    List<ParticipationRequest> findAllByRequester_Id(long userId);

    Object findByRequesterIdAndEventId(long userId, long eventId);
}
