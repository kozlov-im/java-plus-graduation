package ru.practicum.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaClient extends AutoCloseable {

    Consumer<String, UserActionAvro> getUserActionConsumer();

    Consumer<String, EventSimilarityAvro> getEventSimilarityConsumer();

    void stop();

    @Override
    default void close() throws Exception {
        stop();
    }
}
