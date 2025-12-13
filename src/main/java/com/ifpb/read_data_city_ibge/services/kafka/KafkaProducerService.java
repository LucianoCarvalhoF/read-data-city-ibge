package com.ifpb.read_data_city_ibge.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaProducer<String, String> kafkaProducer;

    public void sendMessage(String topicName, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, null, message);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (Objects.isNull(exception)) {
                log.info("Message published in topic: {}, partition: {}, offset: {}", metadata.topic(), metadata.partition(), metadata.offset());
            } else {
                log.error("Posting failed for the topic {}. Exception: {}", topicName, exception.getMessage());
            }
        });
    }
}