package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.handler.EventSimilarityHandle;
import ru.practicum.kafka.KafkaClient;
import ru.practicum.kafka.TopicsConfig;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {
    private final KafkaClient kafkaClient;
    private final TopicsConfig topicsConfig;
    private final EventSimilarityHandle eventSimilarityHandle;

    @Override
    public void run() {
        Consumer<String, EventSimilarityAvro> consumer = kafkaClient.getEventSimilarityConsumer();

        try (consumer) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(List.of(topicsConfig.getSimilarityTopic()));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(Duration.ofMillis(topicsConfig.getConsumerAttemptTimeout()));

                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    EventSimilarityAvro eventSimilarityAvro = record.value();
                    eventSimilarityHandle.handle(eventSimilarityAvro);
                    log.info("Analyzer got eventSimilarity from {} {}", topicsConfig.getSimilarityTopic(), eventSimilarityAvro);
                }
                consumer.commitSync();
            }

        } catch (Exception e) {
            log.error("Event similarity consumer got an error: ", e);
        }
    }
}