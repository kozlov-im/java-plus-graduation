package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.handler.UserActionHandler;
import ru.practicum.kafka.KafkaClient;
import ru.practicum.kafka.TopicsConfig;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {
    private final KafkaClient kafkaClient;
    private final TopicsConfig topicsConfig;
    private final UserActionHandler userActionHandler;

    @Override
    public void run() {

        try (Consumer<String, UserActionAvro> consumer = kafkaClient.getUserActionConsumer();) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(List.of(topicsConfig.getUserActionsTopic()));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(topicsConfig.getConsumerAttemptTimeout()));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    UserActionAvro userActionAvro = record.value();
                    log.info("Analyzer got userAction from {} {}", topicsConfig.getUserActionsTopic(), userActionAvro);
                    userActionHandler.handle(userActionAvro);
                }
                consumer.commitSync();
            }

        } catch (Exception e) {
            log.error("User action consumer got an error: ", e);
        }
    }
}