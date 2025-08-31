package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.KafkaClient;
import ru.practicum.kafka.TopicsConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class AggregationStarter {

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final RecordHandler recordHandler;
    private final KafkaClient kafkaClient;
    private final TopicsConfig topicsConfig;

    public void start() {

        Consumer<String, UserActionAvro> consumer = kafkaClient.getConsumer();
        Producer<String, SpecificRecordBase> producer = kafkaClient.getProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера.");
            consumer.wakeup(); // инициируем прерывание блокировки poll()
        }));

        try {
            consumer.subscribe(List.of(topicsConfig.getUserActionsTopic()));

            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(topicsConfig.getConsumerAttemptTimeout()));
                int count = 0;

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    log.info("userActionAvro was received {}", record);
                    Optional<List<EventSimilarityAvro>> eventSimilarityAvroOptionalList = recordHandler.handle(record.value());
                    if (eventSimilarityAvroOptionalList.isPresent()) {
                        List<EventSimilarityAvro> eventSimilarityAvroList = eventSimilarityAvroOptionalList.get();

                        for (EventSimilarityAvro eventSimilarityAvro : eventSimilarityAvroList) {

                            ProducerRecord<String, SpecificRecordBase> producerRecord =
                                    new ProducerRecord<>(
                                            topicsConfig.getSimilarityTopic(),
                                            null,
                                            eventSimilarityAvro.getTimestamp().toEpochMilli(),
                                            null,
                                            eventSimilarityAvro);
                            producer.send(producerRecord);

                            log.info("Into {} send eventSimilarity A = {} and B = {} eventSimilarityAvro {}",
                                    topicsConfig.getSimilarityTopic(), eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB(),
                                    eventSimilarityAvro);
                        }
                    }
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.info("user actions get error", e);
        } finally {
            try (consumer; producer) {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("consumer was closed");
                log.info("producer was closed");
            }
        }
    }

    private void manageOffsets(ConsumerRecord<String, UserActionAvro> record, int count,
                               Consumer<String, UserActionAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error during offsets fix: {}", offsets, exception);
                }
            });
        }

    }
}
