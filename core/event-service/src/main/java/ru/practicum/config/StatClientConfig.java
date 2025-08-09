package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatClient;

@Configuration
public class StatClientConfig {

    @Value("${client.url}")
    private String clientUrl;

    @Bean
    public StatClient statClient() {
        return new StatClient(clientUrl);

    }
}
