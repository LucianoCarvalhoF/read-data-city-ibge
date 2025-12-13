package com.ifpb.read_data_city_ibge.controllers;

import com.ifpb.read_data_city_ibge.models.ReadDataRequestModel;
import com.ifpb.read_data_city_ibge.services.IbgeHtmlReadService;
import com.ifpb.read_data_city_ibge.services.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ReadDataController {

    @Value("${kafka.topic.read-data}")
    private String kafkaTopic;

    private final IbgeHtmlReadService IbgeHtmlReadService;
    private final KafkaProducerService kafkaProducerService;

    public static final String YEAR_NOT_FOUND = "The value of `year` should not be greater than the current year.";

    @PostMapping("/read-data")
    public void readData(@Valid @RequestBody ReadDataRequestModel requestModel) throws Exception {
        if (requestModel.getYear() > LocalDate.now().getYear()) {
            log.error(YEAR_NOT_FOUND);
        }

        String ibgeUrl = IbgeHtmlReadService.readData(requestModel);
        kafkaProducerService.sendMessage(kafkaTopic, ibgeUrl);
    }
}