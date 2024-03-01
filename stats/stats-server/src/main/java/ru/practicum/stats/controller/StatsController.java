package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.dto.stats.ViewStats;
import ru.practicum.stats.service.StatsService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@RequestBody EndpointHit endpointHit) {
        log.info("Информация сохранена {}", endpointHit);
        statsService.add(endpointHit);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStats> getStats(@RequestParam String start,
                                    @RequestParam String end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Выполнен запрос на получение статистики с {} по {}", start, end);
        return statsService.getStats(start, end, uris, unique);
    }
}
