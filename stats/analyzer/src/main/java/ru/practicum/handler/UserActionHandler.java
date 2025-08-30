package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.AnalyzerMapper;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionHandler {

    private final UserActionRepository userActionRepository;
    private final AnalyzerMapper analyzerMapper;

    public void handle(UserActionAvro userActionAvro) {
        UserAction userAction = analyzerMapper.mapToUserAction(userActionAvro);
        Optional<UserAction> previousUserActionOptional = userActionRepository.findByUserIdAndEventId(userAction.getUserId(), userAction.getEventId());

        boolean shouldSave = previousUserActionOptional
                .map(previousUserAction -> previousUserAction.getWeight() < userAction.getWeight())
                .orElse(true);
        if (shouldSave) {
            userActionRepository.save(userAction);
            log.info("userAction was save {}", userAction);
        }
    }
}
