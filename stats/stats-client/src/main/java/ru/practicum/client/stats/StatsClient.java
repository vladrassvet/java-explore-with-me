package ru.practicum.client.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.stats.EndpointHit;
import ru.practicum.dto.stats.ViewStats;

import java.util.List;
import java.util.Map;

@Service
public class StatsClient {
    private final RestTemplate restTemplate;

    public StatsClient() {
        restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    @Value("${stats.server.url}")
    private String uri;

    public void hit(EndpointHit endpointHit) {
        restTemplate.postForObject(uri + "/hit", endpointHit, Object.class);
    }

    public List<ViewStats> getStats(String start, String end, List<String> uris, Boolean unique) {
        final String url = uri + "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        ResponseEntity<List<ViewStats>> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                },
                Map.of("start", start,
                        "end", end,
                        "uris", uris,
                        "unique", unique)
        );
        return responseEntity.getBody();
    }
}
