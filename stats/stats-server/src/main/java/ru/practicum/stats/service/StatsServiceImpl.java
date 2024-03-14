package ru.practicum.stats.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.dto.stats.ViewStats;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.mapper.Mapper;
import ru.practicum.stats.model.Stats;
import ru.practicum.stats.repository.StateRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StateRepository repository;

    @Override
    public void add(EndpointHit endpointHit) {
        Stats stats = Mapper.toStats(endpointHit);
        repository.save(stats);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end) || start.equals(end)) {
            throw new ValidationException("Некорректно заданы временные заданы параметры поиска");
        }
        List<ViewStats> stats;
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                stats = repository.getStatsUnique(start, end);
            } else {
                stats = repository.getStats(start, end);
            }
        } else {
            if (!unique) {
                stats = repository.getStatsForUri(start, end, uris);
            } else {
                stats = repository.getStatsForUriUnique(start, end, uris);
            }
        }
        log.info("Возвращаем запрос {}", stats);
        return stats;
    }
}
