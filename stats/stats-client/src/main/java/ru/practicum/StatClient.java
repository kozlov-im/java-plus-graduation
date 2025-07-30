package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

@Service
@Slf4j
public class StatClient {
    private final RestClient restClient;

    public StatClient() {
        String clientUrl = "http://stats-server:9090";
        this.restClient = RestClient.builder().baseUrl(clientUrl).build();
    }

    public void sendHit(EndpointHitDto endpointHitDto) {
        try {
            restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHitDto)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Статистика посещений записана");
        } catch (Exception e) {
            log.error(getLogMessage("запись") + e.getMessage());
            throw new RuntimeException(getLogMessage("запись") + e);
        }
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        try {
            var uriBuilder = UriComponentsBuilder.fromPath("/stats")
                    .queryParam("start", start)
                    .queryParam("end", end)
                    .queryParam("unique", unique);
            if (uris != null && !uris.isEmpty()) {
                uris.forEach(uri -> uriBuilder.queryParam("uris", uri));
            }
            log.info("Статистика посещений получена");
            log.info("Параметры запроса - start: {}, end: {}, unique: {}, uris: {}", start, end, unique, uris);
            log.info("Запрос URI: {}", uriBuilder.build().toUriString());
            return restClient.get()
                    .uri(uriBuilder.build().toUriString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                    });
        } catch (Exception e) {
            log.error(getLogMessage("чтение") + e.getMessage());
            throw new RuntimeException(getLogMessage("чтение") + e);
        }
    }

    public String getLogMessage(String goal) {
        if (goal.equals("запись")) {
            return "Ошибка записи статистики посещений: ";
        } else if (goal.equals("чтение")) {
            return "Ошибка чтения статистики посещений: ";
        } else
            return "Ошибка статистики посещений: ";
    }
}