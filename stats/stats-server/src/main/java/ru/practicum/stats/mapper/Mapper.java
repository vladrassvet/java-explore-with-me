package ru.practicum.stats.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.stats.model.Stats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {
    public static Stats toStats(EndpointHit endpointHit) {
        return Stats.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(LocalDateTime.parse(endpointHit.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}
