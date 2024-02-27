package ru.practicum.stats.service;

import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.dto.stats.ViewStats;

import java.util.List;

public interface StatsService {

    void add(EndpointHit endpointHit);

    List<ViewStats> getStats(String start, String end, List<String> uris, Boolean unique);

}
