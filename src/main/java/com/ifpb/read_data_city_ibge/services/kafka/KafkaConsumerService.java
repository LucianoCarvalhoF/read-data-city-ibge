package com.ifpb.read_data_city_ibge.services.kafka;

import com.ifpb.read_data_city_ibge.services.DataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class KafkaConsumerService {

    private final DataProcessingService dataProcessingService;
    private final RetryTemplate retryTemplate;

    @KafkaListener(
            topics = "${kafka.topic.read-data}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "batchListenerContainerFactory")
    public void listenInBatch(List<ConsumerRecord<String, String>> records) {

        for (ConsumerRecord<String, String> record : records) {
            final String linkFile = record.value();

            try {
                retryTemplate.execute(getObjectRuntimeExceptionRetryCallback(record, linkFile),
                        getObjectRecoveryCallback(linkFile));
            } catch (Exception e) {
                log.error("Fatal error during batch processing: ", e);
            }
        }
    }

    private static RecoveryCallback<Object> getObjectRecoveryCallback(String linkFile) {
        return context -> {
            log.error("Critical failure {} attempts were exhausted. Link not processed: {}", context.getRetryCount(), linkFile, context.getLastThrowable());
            return null;
        };
    }

    private RetryCallback<Object, RuntimeException> getObjectRuntimeExceptionRetryCallback(ConsumerRecord<String, String> record, String linkFile) {
        return context -> {
            log.info("Attempt {} to process the link, Offset {}: {}", context.getRetryCount() + 1, record.offset(), linkFile);
            dataProcessingService.importDrive(linkFile);
            log.info("Processing completed successfully, Offset: {}", record.offset());
            return null;
        };
    }
}