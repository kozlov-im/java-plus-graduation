package ru.practicum;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto requestProto = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        List<RecommendedEventProto> recommendedEventProtoList = new ArrayList<>();
        analyzerStub.getRecommendationsForUser(requestProto).forEachRemaining(recommendedEventProtoList::add);
        return recommendedEventProtoList;
    }

    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventRequestProto requestProto = SimilarEventRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        List<RecommendedEventProto> similarEvents = new ArrayList<>();
        analyzerStub.getSimilarEvent(requestProto).forEachRemaining(similarEvents::add);
        return similarEvents;
    }

    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto requestProto = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        Map<Long, Double> interactionsCount = new HashMap<>();
        analyzerStub.getInteractionsCount(requestProto)
                .forEachRemaining(event -> interactionsCount.put(event.getEventId(), event.getScore()));
        return interactionsCount;
    }
}
