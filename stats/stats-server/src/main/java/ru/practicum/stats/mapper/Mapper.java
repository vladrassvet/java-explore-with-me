package ru.practicum.stats.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.stats.model.Stats;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {

    public static Stats toStats(EndpointHit endpointHit) {
        return Stats.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp()).build();
    }
}
