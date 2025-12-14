package com.ifpb.read_data_city_ibge.config;

import com.ifpb.read_data_city_ibge.config.util.HerokuKafkaPropertiesBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    private Map<String, Object> mergeHerokuProperties(Map<String, Object> springProps) throws IOException {
        Map<String, Object> herokuProps = new HashMap<>();
        HerokuKafkaPropertiesBuilder.build().forEach((key, value) -> herokuProps.put(key.toString(), value));

        Map<String, Object> mergedProps = new HashMap<>(springProps);
        mergedProps.putAll(herokuProps);

        return mergedProps;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() throws IOException {
        Map<String, Object> springProducerProps = kafkaProperties.buildProducerProperties();
        Map<String, Object> configs = mergeHerokuProperties(springProducerProps);

        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() throws IOException {
        Map<String, Object> springConsumerProps = kafkaProperties.buildConsumerProperties();
        Map<String, Object> configs = mergeHerokuProperties(springConsumerProps);

        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> batchListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);

        return factory;
    }
}