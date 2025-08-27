package ru.practicum.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("topics")
@Data
public class TopicsConfig {
    private String userActionsTopic;
    private String similarityTopic;
    private int consumerAttemptTimeout;
}
