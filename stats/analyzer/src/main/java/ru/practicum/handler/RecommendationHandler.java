package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.mapper.AnalyzerMapper;

import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;


import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendationHandler {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;
    private final AnalyzerMapper analyzerMapper;

    public List<RecommendedEventProto> handle(UserPredictionsRequestProto requestProto) {
        List<UserAction> interactions = userActionRepository.findByUserId(requestProto.getUserId());
        if (interactions.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> recentEvents = interactions.stream()
                .sorted(Comparator.comparing(UserAction::getTimestamp).reversed())
                .limit(requestProto.getMaxResults())
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        List<EventSimilarity> similarities = new ArrayList<>();
        for (Long eventId : recentEvents) {
            similarities.addAll(eventSimilarityRepository.findByEventAOrEventB(eventId, eventId));
        }

        Set<Long> userInteractions = interactions.stream().map(UserAction::getEventId).collect(Collectors.toSet());

        return similarities.stream()
                .filter(eventSimilarity -> !userInteractions.contains(eventSimilarity.getEventA()) ||
                        !userInteractions.contains(eventSimilarity.getEventB()))
                .sorted(Comparator.comparing(EventSimilarity::getScore).reversed())
                .limit(requestProto.getMaxResults())
                .map(eventSimilarity -> {
                    long recommendedEvent = userInteractions.contains(eventSimilarity.getEventA())
                            ? eventSimilarity.getEventB() : eventSimilarity.getEventA();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(recommendedEvent)
                            .setScore(eventSimilarity.getScore())
                            .build();

                }).toList();
    }

    public List<RecommendedEventProto> handle(SimilarEventRequestProto requestProto) {
        List<EventSimilarity> similarities = eventSimilarityRepository.findByEventAOrEventB(requestProto.getEventId(), requestProto.getEventId());

        Set<Long> userInteractions = userActionRepository.findByUserId(requestProto.getUserId()).stream()
                .map(UserAction::getEventId).collect(Collectors.toSet());

        return similarities.stream()
                .filter(eventSimilarity -> !userInteractions.contains(eventSimilarity.getEventA()) ||
                        !userInteractions.contains(eventSimilarity.getEventB()))
                .sorted(Comparator.comparing(EventSimilarity::getScore).reversed())
                .limit(requestProto.getMaxResults())
                .map(eventSimilarity -> {
                    long recommendedEvent = eventSimilarity.getEventA().equals(requestProto.getEventId()) ?
                            eventSimilarity.getEventB() : eventSimilarity.getEventA();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(recommendedEvent)
                            .setScore(eventSimilarity.getScore())
                            .build();

                }).toList();
    }

    public List<RecommendedEventProto> handle(InteractionsCountRequestProto requestProto) {
        List<RecommendedEventProto> recommendedEventProtoList = new ArrayList<>();
        for (long eventId : requestProto.getEventIdList()) {
            double weightSum = userActionRepository.findByUserId(eventId)
                    .stream().mapToDouble(UserAction::getWeight).sum();
            RecommendedEventProto recommendedEventProto = RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(weightSum)
                    .build();
            recommendedEventProtoList.add(recommendedEventProto);
        }
        return recommendedEventProtoList;
    }
}
