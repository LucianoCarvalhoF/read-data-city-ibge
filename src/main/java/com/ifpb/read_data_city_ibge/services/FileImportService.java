package com.ifpb.read_data_city_ibge.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileImportService {

    private final GoogleDriveService driveUploadService;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public void importFileToDrive(String fileUrl) throws Exception {
        File tempFile = null;
        String driveFileId;

        try {
            log.info("Starting import to Drive for the link: {}", fileUrl);

            tempFile = downloadFile(fileUrl);
            log.info("Download complete. Temporary file created on: {}", readFilename(tempFile.getAbsolutePath()));

            driveFileId = driveUploadService.uploadFile(tempFile, "*/*");
            log.info("Import completed successfully. Drive ID: {}", driveFileId);
        } catch (IOException | GeneralSecurityException e) {
            log.error("Error during the download or upload process from Drive to the link. {}", fileUrl, e);
            throw new Exception("Failed to import file to Google Drive.", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                Files.delete(tempFile.toPath());
                log.info("Temporary file deleted: {}", tempFile.getName());
            }
        }
    }

    private File downloadFile(String url) throws IOException {
        URL website = new URL(url);

        String fileName = readFilename(url);

        Path tempPath = Path.of(TEMP_DIR, fileName);
        File tempFile = tempPath.toFile();

        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        return tempFile;
    }

    private static String readFilename(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}