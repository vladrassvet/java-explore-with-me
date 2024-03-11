package ru.practicum.ewm.service.view;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.stats.StatsClient;
import ru.practicum.dto.stats.ViewStats;
import ru.practicum.ewm.model.event.Event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ViewService {

    private StatsClient statsClient;

    public Map<Long, Integer> getViews(List<Event> result) {
        if (result.isEmpty() || result.get(0).getPublishedOn() == null) {
            return Collections.emptyMap();
        }
        LocalDateTime start;
        if (result.size() > 1) {
            start = result.stream()
                    .map(Event::getPublishedOn)
                    .min(LocalDateTime::compareTo)
                    .get();
        } else {
            start = result.get(0).getPublishedOn();
        }
        String[] uris = result.stream()
                .map(e -> e.getId())
                .map(e -> String.format("/events/%d", e))
                .toArray(String[]::new);
        List<ViewStats> viewStatsList =
                statsClient.getStats(start.minusMinutes(1),
                        LocalDateTime.now(), uris, true);
        Map<Long, Integer> viewsMap = new HashMap<>();
        for (ViewStats viewStats : viewStatsList) {
            String index = viewStats.getUri().substring(8);
            viewsMap.put(Long.parseLong(index), Math.toIntExact(viewStats.getHits()));
        }
        return viewsMap;
    }
}
