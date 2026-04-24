package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {

    private final RestTemplate rest;
    private final String serverUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplate rest) {
        this.rest = rest;
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto hitDto) {
        return rest.postForEntity(serverUrl + "/hit", hitDto, Object.class);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>(Map.of(
                "start", start.format(FORMATTER),
                "end", end.format(FORMATTER),
                "unique", unique
        ));

        StringBuilder pathBuilder = new StringBuilder(serverUrl + "/stats?start={start}&end={end}&unique={unique}");

        if (uris != null && !uris.isEmpty()) {
            pathBuilder.append("&uris={uris}");
            parameters.put("uris", String.join(",", uris));
        }

        return rest.getForEntity(pathBuilder.toString(), Object.class, parameters);
    }
}