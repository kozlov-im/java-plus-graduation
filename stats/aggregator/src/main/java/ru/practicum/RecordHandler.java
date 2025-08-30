package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecordHandler {

    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    private final Map<Long, Map<Long, Double>> eventUsersWeigths = new HashMap<>();
    private final Map<Long, Double> eventWeightSums = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public Optional<List<EventSimilarityAvro>> handle(UserActionAvro userActionAvro) {

        long eventId = userActionAvro.getEventId();
        long userId = userActionAvro.getUserId();
        double weight = switch (userActionAvro.getActionType()) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };

        return Optional.ofNullable(updateWeighs(eventId, userId, weight, userActionAvro.getTimestamp()));
    }

    private List<EventSimilarityAvro> updateWeighs(long eventId, long userId, double weight, Instant timestamp) {

        Map<Long, Double> userWeights = eventUsersWeigths.computeIfAbsent(eventId, e -> new HashMap<>());

        Double previousWeight = userWeights.get(userId);
        double actualPreviousWeighs = (previousWeight != null) ? previousWeight : 0.0;

        userWeights.merge(userId, weight, Math::max);
        double currentWeight = userWeights.get(userId);
        System.out.println("eventUsersWeigths" + eventUsersWeigths);

        if (currentWeight != actualPreviousWeighs) {
            double eventWeightSum = eventWeightSums.getOrDefault(eventId, 0.0);
            eventWeightSum += (currentWeight - actualPreviousWeighs);
            eventWeightSums.put(eventId, eventWeightSum);
            System.out.println("eventWeightSums " + eventWeightSums);
            return calculateSimilarity(eventId, userId, timestamp, actualPreviousWeighs);
        }
        return null;
    }

    private List<EventSimilarityAvro> calculateSimilarity(long eventA, long userId, Instant timestamp, double previousWeight) {
        List<EventSimilarityAvro> eventSimilarityAvroList = new ArrayList<>();
        for (long eventB : eventWeightSums.keySet()) {

            double sMin;

            if (eventA == eventB) continue;

            long firstEvent = Math.min(eventA, eventB);
            //System.out.println("firstEvent " + firstEvent);
            long secondEvent = Math.max(eventA, eventB);
            //System.out.println("secondEvent " + secondEvent);

            if ((eventUsersWeigths.get(firstEvent).get(userId) == null ||
                    eventUsersWeigths.get(firstEvent).get(userId).equals(0.0)) ||
                    (eventUsersWeigths.get(secondEvent).get(userId) == null ||
                            eventUsersWeigths.get(secondEvent).get(userId).equals(0.0))) {
                continue;
            }
            if (minWeightsSums.getOrDefault(firstEvent, new HashMap<>()).getOrDefault(secondEvent, -1.0) == -1.0) {
                Map<Long, Double> event1UsersWeights = eventUsersWeigths.get(firstEvent);
                Map<Long, Double> event2UsersWeights = eventUsersWeigths.get(secondEvent);
                Set<Long> allUsers = new HashSet<>();
                allUsers.addAll(event1UsersWeights.keySet());
                allUsers.addAll(event2UsersWeights.keySet());
                Map<Long, Double> minWeighs = new HashMap<>();
                for (Long internalUserId : allUsers) {
                    double weight1 = event1UsersWeights.getOrDefault(internalUserId, 0.0);
                    double weight2 = event2UsersWeights.getOrDefault(internalUserId, 0.0);
                    minWeighs.put(internalUserId, Math.min(weight1, weight2));
                }
                sMin = minWeighs.values().stream().mapToDouble(Double::doubleValue).sum();
                minWeightsSums.computeIfAbsent(firstEvent, e -> new HashMap<>()).put(secondEvent, sMin);
                System.out.println("minWeightsSums " + minWeightsSums);
            } else {
                double oldMinWeightSum = minWeightsSums.get(firstEvent).get(secondEvent);
                //System.out.println("oldMinWeights " + oldMinWeightSum);

                //System.out.println("previousUserWeight " + previousWeight);
                double newWeight;
                double weightForComparison;
                if (firstEvent == eventA) {
                    newWeight = eventUsersWeigths.getOrDefault(firstEvent, new HashMap<>()).getOrDefault(userId, 0.0);
                    weightForComparison = eventUsersWeigths.getOrDefault(secondEvent, new HashMap<>()).getOrDefault(userId, 0.0);
                } else {
                    newWeight = eventUsersWeigths.getOrDefault(secondEvent, new HashMap<>()).getOrDefault(userId, 0.0);
                    weightForComparison = eventUsersWeigths.getOrDefault(firstEvent, new HashMap<>()).getOrDefault(userId, 0.0);
                }
                //System.out.println("newWeight " + newWeight);
                //System.out.println("weightForComparison " + weightForComparison);

                double previousMinWeight = Math.min(weightForComparison, previousWeight);
                double currentMinWeight = Math.min(weightForComparison, newWeight);

                //System.out.println("previousMinWeight " + previousMinWeight);
                //System.out.println("currentMinWeight " + currentMinWeight);
                sMin = oldMinWeightSum - previousMinWeight + currentMinWeight;
                minWeightsSums.get(firstEvent).put(secondEvent, sMin);
                //System.out.println("minWeightsSums " + minWeightsSums);
            }
            //System.out.println("sMin " + sMin);
            double sA = eventWeightSums.getOrDefault(firstEvent, 0.0);
            double sB = eventWeightSums.getOrDefault(secondEvent, 0.0);
            //System.out.println("sA " + sA);
            //System.out.println("sB " + sB);

            double similarity = sMin / (Math.sqrt(sA) * Math.sqrt(sB));

            //System.out.println("similarity " + similarity);

            EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                    .setEventA(firstEvent)
                    .setEventB(secondEvent)
                    .setScore(similarity)
                    .setTimestamp(timestamp)
                    .build();
            eventSimilarityAvroList.add(eventSimilarityAvro);

        }
        //System.out.println("eventSimilarityAvroList " + eventSimilarityAvroList);
        return eventSimilarityAvroList;
    }
}