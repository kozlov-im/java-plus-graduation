package ru.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaClient extends AutoCloseable {

    Producer<String, SpecificRecordBase> getProducer();

    Consumer<String, UserActionAvro> getConsumer();

    void stop();

    @Override
    default void close() throws Exception {
        stop();
    }
}
