package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.stats.ViewStats;
import ru.practicum.stats.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StateRepository extends JpaRepository<Stats, Long> {
    @Query(value = "SELECT new ru.practicum.dto.stats.ViewStats(s.app, s.uri, COUNT(s.ip)) " +
            "FROM Stats s WHERE s.uri in :uris and s.timestamp between :start and :end" +
            " GROUP BY s.app, s.uri ORDER BY COUNT(s.ip) DESC")
    List<ViewStats> getStatsForUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT new ru.practicum.dto.stats.ViewStats(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
            "FROM Stats s WHERE s.uri in :uris and s.timestamp between :start and :end" +
            " GROUP BY s.app, s.uri ORDER BY COUNT(s.ip) DESC")
    List<ViewStats> getStatsForUriUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT new ru.practicum.dto.stats.ViewStats(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
            "FROM Stats s WHERE s.timestamp between :start and :end GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.ip) DESC")
    List<ViewStats> getStatsUnique(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT new ru.practicum.dto.stats.ViewStats(s.app, s.uri, COUNT(s.ip)) " +
            "FROM Stats s WHERE s.timestamp between :start and :end GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.ip) DESC")
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end);
}
