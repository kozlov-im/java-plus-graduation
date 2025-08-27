package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.AnalyzerMapper;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionHandler {

    private final UserActionRepository userActionRepository;
    private final AnalyzerMapper analyzerMapper;

    public void handle(UserActionAvro userActionAvro) {
        UserAction userAction = analyzerMapper.mapToUserAction(userActionAvro);
        userActionRepository.save(userAction);
        log.info("userAction was save {}", userAction);

    }
}
