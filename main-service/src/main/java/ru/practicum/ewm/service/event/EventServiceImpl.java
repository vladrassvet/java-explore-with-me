package ru.practicum.ewm.service.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.stats.StatsClient;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.ewm.configurations.AppName;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.enums.AdminStateAction;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.ParticipationState;
import ru.practicum.ewm.enums.UserStateAction;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.category.CategoryMapper;
import ru.practicum.ewm.mapper.event.EventMapper;
import ru.practicum.ewm.mapper.location.LocationMapper;
import ru.practicum.ewm.mapper.request.RequestMapper;
import ru.practicum.ewm.mapper.user.UserMapper;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.QEvent;
import ru.practicum.ewm.model.location.Location;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.category.CategoryRepository;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.location.LocationRepository;
import ru.practicum.ewm.repository.request.RequestRepository;
import ru.practicum.ewm.repository.user.UserRepository;
import ru.practicum.ewm.service.view.ViewService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final ViewService viewService;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final AppName name;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<EventFullDto> getEvents_2(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        List<BooleanExpression> conditions = new ArrayList<>();
        List<Event> eventListResult;
        Pageable pageable = PageRequest.of(from / size, size);
        QEvent event = QEvent.event;
        if (users != null) {
            List<User> userList = users.stream().map(user -> userRepository.findById(user).orElseThrow()).collect(Collectors.toList());
            conditions.add(event.initiator.in(userList));
        }
        if (states != null) {
            conditions.add(event.state.in(states));
        }
        if (categories != null) {
            List<Category> categoryList = categoryRepository.findByIdIn(categories);
            if (categories.size() == categoryList.size()) {
                conditions.add(event.category.in(categoryList));
            }
        }
        if (rangeStart != null) {
            conditions.add(event.eventDate.goe(rangeStart));
        }
        if (rangeEnd != null) {
            conditions.add(event.eventDate.loe(rangeEnd));
        }
        if (conditions.isEmpty()) {
            eventListResult = eventRepository.findAll(PageRequest.of(from / size, size)).getContent();
        } else {
            BooleanExpression booleanExpression = conditions.get(0);
            for (int i = 1; i < conditions.size(); i++) {
                booleanExpression = booleanExpression.and(conditions.get(i));
            }
            eventListResult = eventRepository.findAll(booleanExpression, pageable).getContent();
        }
        Map<Long, Integer> viewsMap = viewService.getViews(eventListResult);
        addRequestConfirmed(eventListResult);
        List<EventFullDto> listEventFullDto = eventListResult.stream()
                .map(e -> toEventFullDto(e, e.getConfirmedRequest(), viewsMap.getOrDefault(e.getId(), 0)))
                .collect(Collectors.toList());
        log.info("События найдены {}", listEventFullDto);
        return listEventFullDto;
    }

    @Override
    public EventFullDto updateEvent_1(long eventId, UpdateEventAdminRequest adminRequest) {
        LocalDateTime data = LocalDateTime.now();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдена или недоступна"));
        if (adminRequest.getAnnotation() != null && !adminRequest.getAnnotation().isBlank()) {
            event.setAnnotation(adminRequest.getAnnotation());
        }
        if (adminRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(adminRequest.getCategory()).orElseThrow());
        }
        if (adminRequest.getDescription() != null && !adminRequest.getDescription().isBlank()) {
            event.setDescription(adminRequest.getDescription());
        }
        if (adminRequest.getEventDate() != null) {
            if (adminRequest.getEventDate().isBefore(data.plusHours(1))) {
                throw new ValidationException("Событие не удовлетворяет правилам редактирования");
            }
            event.setEventDate(adminRequest.getEventDate());
        }
        if (adminRequest.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.toLocation(adminRequest.getLocation()));
            event.setLocation(location);
        }
        if (adminRequest.getPaid() != null) {
            event.setPaid(adminRequest.getPaid());
        }
        if (adminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(adminRequest.getParticipantLimit());
        }
        if (adminRequest.getRequestModeration() != null) {
            event.setRequestModeration(adminRequest.getRequestModeration());
        }
        if (adminRequest.getStateAction() != null) {
            if (adminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)
                    && event.getState().equals(EventState.PUBLISHED) ||
                    event.getState().equals(EventState.CANCELED)) {
                throw new ConflictException("Событие не удовлетворяет правилам редактирования");
            }
            if (adminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT)
                    && event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("Событие не удовлетворяет правилам редактирования");
            }
            if (adminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                event.setPublishedOn(data);
                event.setState(EventState.PUBLISHED);
            } else if (adminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }
        if (adminRequest.getTitle() != null && !adminRequest.getTitle().isEmpty()) {
            event.setTitle(adminRequest.getTitle());
        }
        Map<Long, Integer> views = viewService.getViews(List.of(event));
        Integer confirmedRequest = requestRepository.findAllByEventAndStatusOrderByCreated(event,
                ParticipationState.CONFIRMED).size();
        Event updateEvent = eventRepository.save(event);
        EventFullDto eventFullDto = toEventFullDto(updateEvent, confirmedRequest, views.get(eventId));
        log.info("Событие отредактировано {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEvents_1(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from,
                                           int size, HttpServletRequest httpServletRequest) {
        LocalDateTime dateTime = LocalDateTime.now();
        List<BooleanExpression> conditions = new ArrayList<>();
        QEvent event = QEvent.event;
        List<Event> resultEventList;
        Pageable page = PageRequest.of(from / size, size);
        if (sort != null && sort.equals("EVENT_DATE")) {
            page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        }
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = dateTime;
            rangeEnd = rangeStart.plusYears(100);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("запрос составлен некорректно");
        }
        if (text != null) {
            conditions.add(event.annotation.containsIgnoreCase(text).or(event.description.containsIgnoreCase(text)));
        }
        if (categories != null) {
            List<Category> categoryList = categoryRepository.findByIdIn(categories);
            if (categoryList.size() == categories.size()) {
                conditions.add(event.category.in(categoryList));
            }
        }
        if (paid != null) {
            conditions.add(event.paid.eq(paid));
        }
        if (rangeStart != dateTime) {
            conditions.add(event.eventDate.goe(rangeStart));
        }
        if (rangeEnd != null) {
            conditions.add(event.eventDate.loe(rangeEnd));
        }

        BooleanExpression request = event.state.eq(EventState.PUBLISHED);
        if (!conditions.isEmpty()) {
            for (BooleanExpression condition : conditions) {
                request = request.and(condition);
            }
        }
        resultEventList = eventRepository.findAll(request, page).getContent();
        statsClient.hit(new EndpointHit(name.getAppName(), httpServletRequest.getRequestURI(), httpServletRequest.getRemoteAddr(), dateTime.format(formatter)));
        Map<Long, Integer> views = viewService.getViews(resultEventList);
        if (onlyAvailable != null && onlyAvailable) {
            addRequestConfirmed(resultEventList);
            resultEventList.stream().filter(e -> e.getConfirmedRequest() < e.getParticipantLimit()).collect(Collectors.toList());
        }
        List<EventShortDto> eventShortDtos = resultEventList.stream()
                .map(e -> EventMapper.toEventShortDto(e, e.getConfirmedRequest(),
                        CategoryMapper.toDto(e.getCategory()),
                        UserMapper.toUserShortDto(e.getInitiator()), views))
                .collect(Collectors.toList());
        if (sort != null && sort.equals("VIEWS")) {
            eventShortDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .collect(Collectors.toList());
        }
        log.info("События найдены {}", eventShortDtos);
        return eventShortDtos;
    }

    @Override
    public EventFullDto getEvent_1(long id, HttpServletRequest httpServletRequest) {
        LocalDateTime dateTime = LocalDateTime.now();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или недоступно"));
        if (event.getState().equals(EventState.PENDING) || event.getState().equals(EventState.CANCELED)) {
            throw new NotFoundException("Запрос составлен некорректно");
        }
        statsClient.hit(new EndpointHit(name.getAppName(), httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(), dateTime.format(formatter)));
        Map<Long, Integer> views = viewService.getViews(List.of(event));
        Integer confirmedRequest = requestRepository.findAllByEventAndStatusOrderByCreated(event,
                ParticipationState.CONFIRMED).size();
        EventFullDto eventFullDto = toEventFullDto(event, confirmedRequest, views.get(event.getId()));
        log.info("Событие найдено {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getListOfCurrentUserEvents(long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден или недоступн"));
        List<Event> eventList = eventRepository.findAllByInitiatorOrderById(user, PageRequest.of(from / size, size));
        Map<Long, Integer> views = viewService.getViews(eventList);
        addRequestConfirmed(eventList);
        List<EventShortDto> resultList = eventList.stream().map(e -> EventMapper.toEventShortDto(e, e.getConfirmedRequest(),
                CategoryMapper.toDto(e.getCategory()),
                UserMapper.toUserShortDto(e.getInitiator()),
                views)).collect(Collectors.toList());
        log.info("Получение событий, добавленных текущим пользователем {}", resultList);
        return resultList;
    }

    @Override
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Событие не удовлетворяет правилам создания");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найдена или недоступна"));
        Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена или недоступна"));
        Event event = eventRepository.save(EventMapper.toEvent(newEventDto, category, LocalDateTime.now(), user, location));
        Map<Long, Integer> views = viewService.getViews(List.of(event));
        Integer confirmedRequest = requestRepository.findAllByEventAndStatusOrderByCreated(event,
                ParticipationState.CONFIRMED).size();
        EventFullDto eventFullDto = toEventFullDto(event, confirmedRequest, views.get(event.getId()));
        log.info("Событие добавлено {}", event);
        return eventFullDto;
    }

    @Override
    public EventFullDto getEventById(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или недоступно"));
        if (event.getInitiator().getId() != userId) {
            throw new ValidationException("Запрос составлен некорректно");
        }
        Map<Long, Integer> views = viewService.getViews(List.of(event));
        Integer confirmedRequest = requestRepository.findAllByEventAndStatusOrderByCreated(event,
                ParticipationState.CONFIRMED).size();
        EventFullDto eventFullDto = toEventFullDto(event, confirmedRequest, views.get(event.getId()));
        log.info("Событие найдено {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или недоступно"));
        LocalDateTime dateTime = LocalDateTime.now();
        if (userId != event.getInitiator().getId()) {
            throw new ValidationException("Запрос составлен некорректно");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие не удовлетворяет правилам редактирования");
        }
        if (updateEventUserRequest.getAnnotation() != null && !updateEventUserRequest.getAnnotation().isEmpty()) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(updateEventUserRequest.getCategory()));
        }
        if (updateEventUserRequest.getDescription() != null && !updateEventUserRequest.getDescription().isEmpty()) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(dateTime.plusHours(2))) {
                throw new ValidationException("Событие не удовлетворяет правилам редактирования");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.toLocation(updateEventUserRequest.getLocation()));
            event.setLocation(location);
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
        if (updateEventUserRequest.getTitle() != null && !updateEventUserRequest.getTitle().isEmpty()) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        Map<Long, Integer> views = viewService.getViews(List.of(event));
        Integer confirmedRequest = requestRepository.findAllByEventAndStatusOrderByCreated(event, ParticipationState.CONFIRMED).size();
        Event updateEvent = eventRepository.save(event);
        EventFullDto eventFullDtoUpdate = toEventFullDto(updateEvent, confirmedRequest, views.get(updateEvent.getId()));
        log.info("Изменение события добавленного текущим пользователем {}", userId);
        log.info("Событие обновлено {}", eventFullDtoUpdate);
        return eventFullDtoUpdate;
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или недоступно"));
        if (userId != event.getInitiator().getId()) {
            throw new ValidationException("Запрос составлен некорректно");
        }
        List<ParticipationRequest> requestList = requestRepository.findAllByEvent(event);
        List<ParticipationRequestDto> listResult = requestList.stream().map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
        log.info("Найдены запросы на участие {}", listResult);
        return listResult;
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(long userId, long eventId,
                                                              EventRequestStatusUpdateRequest eventRequest) {
        if (eventRequest == null) {
            throw new ConflictException("Запрос составлен некорректно");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено или недоступно"));
        if (userId != event.getInitiator().getId()) {
            throw new ValidationException("Запрос составлен некорректно");
        }
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Запрос составлен некорректно");
        }
        int pending = requestRepository.findAllByEventAndStatusOrderByCreated(event,
                ParticipationState.CONFIRMED).size();
        if (event.getParticipantLimit() <= pending) {
            throw new ConflictException("Достигнут лимит одобренных заявок");
        }
        List<ParticipationRequest> participationRequests = requestRepository.findAllById(eventRequest.getRequestIds());
        List<ParticipationRequest> requestConfirm = new ArrayList<>();
        List<ParticipationRequest> requestRejected = new ArrayList<>();
        if (eventRequest.getStatus().equals(ParticipationState.CONFIRMED)) {
            for (ParticipationRequest participation : participationRequests) {
                if (!participation.getStatus().equals(ParticipationState.PENDING)) {
                    throw new ConflictException("Заявка не в состоянии ожидания");
                }
                if (event.getParticipantLimit() > pending) {
                    participation.setStatus(ParticipationState.CONFIRMED);
                    requestConfirm.add(participation);
                } else {
                    participation.setStatus(ParticipationState.REJECTED);
                    requestRejected.add(participation);
                }
            }
        } else {
            for (ParticipationRequest participation : participationRequests) {
                participation.setStatus(ParticipationState.REJECTED);
                requestRejected.add(participation);
            }
        }
        requestRepository.saveAll(requestConfirm);
        List<ParticipationRequestDto> participationRequestDtosConfirm = requestConfirm.stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
        List<ParticipationRequestDto> participationRequestDtosReject = requestRejected.stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
        EventRequestStatusUpdateResult result = RequestMapper.toEventRequestStatusUpdateResult(
                participationRequestDtosConfirm, participationRequestDtosReject);
        log.info("Статус заявок изменён {}", result);
        return result;
    }

    private void addRequestConfirmed(List<Event> eventListResult) {
        List<ParticipationRequest> participationRequestList = requestRepository.findAllByEventIn(eventListResult);
        for (ParticipationRequest pR : participationRequestList) {
            if (pR.getStatus().equals(ParticipationState.CONFIRMED)) {
                Event event = pR.getEvent();
                event.setConfirmedRequest(event.getConfirmedRequest() + 1);
            }
        }
    }

    private EventFullDto toEventFullDto(Event event, Integer confirmedRequests, Integer views) {
        CategoryDto categoryDto = CategoryMapper.toDto(event.getCategory());
        UserShortDto userDto = UserMapper.toUserShortDto(event.getInitiator());
        LocationDto locationDto = LocationMapper.toLocationDto(event.getLocation());
        return EventMapper.toEventFullDto(event, confirmedRequests, categoryDto, userDto, locationDto, views);
    }
}
