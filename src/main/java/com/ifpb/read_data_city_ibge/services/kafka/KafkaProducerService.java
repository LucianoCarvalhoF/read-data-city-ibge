package com.ifpb.read_data_city_ibge.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    @Value("${kafka.message.default.key}")
    private String defaultKey;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topicName, String message) {
        kafkaTemplate.send(topicName, defaultKey, message)
                .whenComplete(getSendResultThrowableBiConsumer(topicName));

        log.info("Attempting to send message with key: {} to topic: {}", defaultKey, topicName);
    }

    private static BiConsumer<SendResult<String, String>, Throwable> getSendResultThrowableBiConsumer(String topicName) {
        return (result, exception) -> {
            if (exception == null) {
                log.info("Message published in topic: {}, partition: {}, offset: {}, key: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        result.getProducerRecord().key());
            } else {
                String key = Optional.ofNullable(result).map(SendResult::getProducerRecord)
                        .map(ProducerRecord::key).orElse("N/A");
                log.error("Posting failed for the topic {}. Key: {}. Exception: {}", topicName, key, exception.getMessage());
            }
        };
    }
}