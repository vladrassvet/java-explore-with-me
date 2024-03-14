package ru.practicum.ewm.mapper.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.enums.ParticipationState;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .id(participationRequest.getId())
                .created(participationRequest.getCreated())
                .event(participationRequest.getEvent().getId())
                .requester(participationRequest.getRequester().getId())
                .status(participationRequest.getStatus().toString())
                .build();
    }

    public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
            List<ParticipationRequestDto> confirmedRequests,
            List<ParticipationRequestDto> rejectedRequests) {

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    public static ParticipationRequest toParticipationRequest(User user, LocalDateTime now,
                                                              Event event, ParticipationState participationState) {
        return ParticipationRequest.builder()
                .requester(user)
                .created(now)
                .event(event)
                .status(participationState)
                .build();
    }
}
