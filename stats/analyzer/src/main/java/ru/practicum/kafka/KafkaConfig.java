package ru.practicum.kafka;

import jakarta.annotation.PreDestroy;
import lombok.Setter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.deserializer.EventSimilarityDeserialyzer;
import ru.practicum.kafka.deserializer.UserActionDeserializer;

import java.time.Duration;
import java.util.Properties;

@Configuration
@ConfigurationProperties("analyzer.kafka")
@Setter
public class KafkaConfig {

    private String bootstrapServer;
    private String userActionCustomerClientId;
    private String userActionCustomerGroupId;
    private String similarityConsumerClientId;
    private String similarityConsumerGroupId;

    @Bean
    KafkaClient getClient() {
        return new KafkaClient() {

            private Consumer<String, UserActionAvro> userActionConsumer;
            private Consumer<String, EventSimilarityAvro> eventSimilarityConsumer;


            @Override
            public Consumer<String, UserActionAvro> getUserActionConsumer() {
                if (userActionConsumer == null) {
                    initUserActionConsumer();
                }
                return userActionConsumer;
            }

            @Override
            public Consumer<String, EventSimilarityAvro> getEventSimilarityConsumer() {
                if (eventSimilarityConsumer == null) {
                    initEventSimilarityConsumer();
                }
                return eventSimilarityConsumer;
            }

            private void initUserActionConsumer() {
                Properties properties = new Properties();
                properties.put(ConsumerConfig.CLIENT_ID_CONFIG, userActionCustomerClientId);
                properties.put(ConsumerConfig.GROUP_ID_CONFIG, userActionCustomerGroupId);
                properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
                properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
                userActionConsumer = new KafkaConsumer<>(properties);
            }

            private void initEventSimilarityConsumer() {
                Properties properties = new Properties();
                properties.put(ConsumerConfig.CLIENT_ID_CONFIG, similarityConsumerClientId);
                properties.put(ConsumerConfig.GROUP_ID_CONFIG, similarityConsumerGroupId);
                properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
                properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserialyzer.class);
                eventSimilarityConsumer = new KafkaConsumer<>(properties);
            }

            @Override
            public void stop() {
                if (userActionConsumer != null) {
                    userActionConsumer.wakeup();
                    userActionConsumer.close(Duration.ofSeconds(10));
                }
                if (eventSimilarityConsumer != null) {
                    eventSimilarityConsumer.wakeup();
                    eventSimilarityConsumer.close(Duration.ofSeconds(10));
                }
            }

            @PreDestroy
            public void cleanUp() {
                stop();
            }

        };
    }
}
