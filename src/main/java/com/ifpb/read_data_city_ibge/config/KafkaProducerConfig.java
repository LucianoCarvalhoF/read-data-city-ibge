package com.ifpb.read_data_city_ibge.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class KafkaProducerConfig {

    @Value("${bootstrap.servers}")
    private String bootstrapServers;
    @Value("${key.serializer}")
    private String keySerializer;
    @Value("${value.serializer}")
    private String valueSerializer;
    @Value("${acks}")
    private String acks;

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put(ProducerConfig.ACKS_CONFIG, acks);

        return new KafkaProducer<>(props);
    }
}