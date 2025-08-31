package ru.practicum.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("topics")
@NoArgsConstructor
@Setter
@Getter
public class TopicsConfig {
    private String userActionsTopic;
    private String similarityTopic;
    private int consumerAttemptTimeout;
}
