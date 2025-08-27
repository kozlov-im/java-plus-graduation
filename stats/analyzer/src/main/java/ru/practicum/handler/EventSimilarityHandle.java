package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.AnalyzerMapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityHandle {

    private final EventSimilarityRepository eventSimilarityRepository;
    private final AnalyzerMapper analyzerMapper;

    public void handle(EventSimilarityAvro eventSimilarityAvro) {
        EventSimilarity eventSimilarity = analyzerMapper.mapToEventSimilarity(eventSimilarityAvro);
        eventSimilarityRepository.save(eventSimilarity);
        log.info("event similarity was saved {}", eventSimilarity);
    }
}
