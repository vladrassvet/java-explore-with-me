package ru.practicum.ewm.service.event;

import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.enums.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getEvents_2(List<Long> users, List<EventState> states, List<Long> categories,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEvent_1(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getEvents_1(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size,
                                    HttpServletRequest httpServletRequest);

    EventFullDto getEvent_1(long id, HttpServletRequest httpServletRequest);

    List<EventShortDto> getListOfCurrentUserEvents(long userId, Integer from, Integer size);

    EventFullDto addEvent(long userId, NewEventDto newEventDto);

    EventFullDto getEventById(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getEventParticipants(long userId, long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(long userId, long eventId,
                                                       EventRequestStatusUpdateRequest eventRequest);
}
