package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.processor.EventSimilarityProcessor;
import ru.practicum.processor.UserActionProcessor;

@SpringBootApplication
@ConfigurationProperties
public class AnalyzerApp {
    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);
        UserActionProcessor userActionProcessor = context.getBean(UserActionProcessor.class);
        EventSimilarityProcessor eventSimilarityProcessor = context.getBean(EventSimilarityProcessor.class);

        Thread userActionThread  = new Thread(userActionProcessor);
        userActionThread.setName("UserActionHandlerThread");
        userActionThread.start();

        eventSimilarityProcessor.run();
    }
}
