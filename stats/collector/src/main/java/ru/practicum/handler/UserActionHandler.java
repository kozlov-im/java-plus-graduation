package ru.practicum.handler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.KafkaClient;
import ru.practicum.stats.user_action.ActionTypeProto;
import ru.practicum.stats.user_action.UserActionProto;

import java.time.Instant;

@Component
@ConfigurationProperties("topics")
@Data
@Slf4j
public class UserActionHandler {
    private final KafkaClient kafkaClient;
    private String userActionsTopic;

    public void handle(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = map(userActionProto);

        kafkaClient.getProducer().send(new ProducerRecord<>(
                userActionsTopic,
                null,
                userActionAvro.getTimestamp().toEpochMilli(),
                null,
                userActionAvro));
        log.info("Into {} sent userActionAvro {}", userActionsTopic, userActionAvro);
    }

    private UserActionAvro map(UserActionProto userActionProto) {
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(mapActionTypeToAvro(userActionProto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()))
                .build();
    }


    private ActionTypeAvro mapActionTypeToAvro(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> null;
        };
    }
}
