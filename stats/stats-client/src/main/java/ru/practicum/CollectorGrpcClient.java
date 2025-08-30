package ru.practicum;

import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.stats.user_action.ActionTypeProto;
import ru.practicum.stats.user_action.UserActionControllerGrpc;
import ru.practicum.stats.user_action.UserActionProto;

import java.time.Instant;

@Component
@Slf4j
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorStub;

    public void sendUserAction(long userId, long eventId, ActionTypeProto actionTypeProto) {
        try {
            log.info("userId {} set actionType {} to event {}", userId, actionTypeProto, eventId);
            UserActionProto requestProto = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionTypeProto)
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano())
                            .build())
                    .build();
            Empty response = collectorStub.collectUserAction(requestProto);
            log.info("response on request {}", response);

        } catch (Exception e) {
            log.error("error occurred", e);
        }
    }

    public void sendEventView(long userId, long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void sendEventRegistration(long userId, long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void sendEventLike(long userId, long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }
}
