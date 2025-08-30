package ru.practicum.kafka.deserializer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilarityDeserialyzer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventSimilarityDeserialyzer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
