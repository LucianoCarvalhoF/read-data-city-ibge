package com.ifpb.read_data_city_ibge.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessingService {

    private final FileImportService fileImportService;

    public void importDrive(String linkFile) throws RuntimeException {
        log.info("Processing data: {}", linkFile);

        try {
            fileImportService.importFileToDrive(linkFile);
        } catch (Exception e) {
            log.error("Processing data error, url: {}. Exception: {}", linkFile, e.getMessage());
        }
    }
}