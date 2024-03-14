package ru.practicum.ewm.controller.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.event.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getListOfCurrentUserEvents(@PathVariable long userId,
                                                          @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                          @RequestParam(required = false, defaultValue = "10") @Positive Integer size) {
        return eventService.getListOfCurrentUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("данные добавляемого события {}", newEventDto);
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable long userId, @PathVariable long eventId) {
        return eventService.getEventById(userId, eventId);

    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable long userId, @PathVariable long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Новые данные события, {}", updateEventUserRequest);
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable long userId, @PathVariable long eventId) {
        return eventService.getEventParticipants(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable long userId, @PathVariable long eventId,
                                                              @RequestBody(required = false) EventRequestStatusUpdateRequest eventRequest) {
        log.info("Новый статус для заявок на участие в событии текущего пользователя {}", eventRequest);
        return eventService.changeRequestStatus(userId, eventId, eventRequest);
    }
}
