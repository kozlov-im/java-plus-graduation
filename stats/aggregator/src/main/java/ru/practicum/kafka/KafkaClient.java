package ru.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaClient {

    Producer<String, SpecificRecordBase> getProducer();

    Consumer<String, UserActionAvro> getConsumer();
}
