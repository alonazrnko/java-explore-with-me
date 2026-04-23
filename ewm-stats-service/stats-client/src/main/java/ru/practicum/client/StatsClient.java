package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto hitDto) {
        return restTemplate.postForEntity("/hit", hitDto, Object.class);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        StringBuilder pathBuilder = new StringBuilder("/stats?start={start}&end={end}&unique={unique}");
        Map<String, Object> parameters = new java.util.HashMap<>(Map.of(
                "start", startStr,
                "end", endStr,
                "unique", unique
        ));

        if (uris != null && !uris.isEmpty()) {
            pathBuilder.append("&uris={uris}");
            parameters.put("uris", String.join(",", uris));
        }

        return restTemplate.getForEntity(pathBuilder.toString(), Object.class, parameters);
    }
}
