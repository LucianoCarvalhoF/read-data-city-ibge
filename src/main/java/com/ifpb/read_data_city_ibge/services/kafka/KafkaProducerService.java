package com.ifpb.read_data_city_ibge.services.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topicName, String message) {
        kafkaTemplate.send(topicName, message).whenComplete(getSendResultThrowableBiConsumer(topicName));
    }

    private static BiConsumer<SendResult<String, String>, Throwable> getSendResultThrowableBiConsumer(String topicName) {
        return (result, exception) -> {
            if (exception == null) {
                log.info("Message published in topic: {}, partition: {}, offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Posting failed for the topic {}. Exception: {}", topicName, exception.getMessage());
            }
        };
    }
}