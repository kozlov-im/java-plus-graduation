package ru.practicum.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaClient {

    Consumer<String, UserActionAvro> getUserActionConsumer();

    Consumer<String, EventSimilarityAvro> getEventSimilarityConsumer();
}
