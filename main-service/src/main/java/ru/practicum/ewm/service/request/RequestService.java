package ru.practicum.ewm.service.request;

import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto addParticipationRequest(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);
}
