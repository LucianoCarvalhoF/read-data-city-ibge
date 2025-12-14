package com.ifpb.read_data_city_ibge.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleDriveService {
    @Value("${google.oauth.credentials}")
    private String credentialsJson;
    @Value("${google.oauth.redirect.uri}")
    private String redirectUri;
    @Value("${google.drive.folder.id}")
    private String folderId;

    private Drive drive;
    private final GoogleAuthorizationCodeFlow flow;

    public static final String SERVICE_USER_ID = "admin_service_user";
    private static final String APPLICATION_NAME = "uploader-Service";

    @PostConstruct
    @SuppressWarnings("unused")
    private void initializeDriveFlowAndClient() throws Exception {
        log.info("Initializing Google Drive flow and attempting to load existing client.");

        try {
            Credential credential = flow.loadCredential(SERVICE_USER_ID);
            if (credential != null && credential.getAccessToken() != null) {
                this.drive = new Drive.Builder(
                        flow.getTransport(),
                        flow.getJsonFactory(),
                        credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                log.info("Google Drive client initialized successfully from stored credentials.");
            } else {
                log.warn("Stored Google Drive credential not found. Authorization is needed via web endpoint.");
                this.drive = null;
            }
        } catch (IOException e) {
            log.error("Error loading stored Google Drive credential: {}", e.getMessage());
            this.drive = null;
        }
    }

    public void setDriveClient(Credential credential) throws IOException {
        this.drive = new Drive.Builder(
                flow.getTransport(),
                flow.getJsonFactory(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        log.info("Google Drive client successfully configured via web callback.");
    }

    public Drive getDriveClient() {
        return Optional.ofNullable(this.drive).orElseThrow(
                () -> new IllegalStateException("Google Drive client is not initialized. Authorization is pending."));
    }

    public String uploadFile(java.io.File localFile, String mimeType) throws Exception {
        File fileMetadata = new File();
        fileMetadata.setName(localFile.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(mimeType, localFile);

        File uploadedFile = getDriveClient().files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink")
                .execute();

        log.info("Upload successful! File '{}' ID: {}", uploadedFile.getName(), uploadedFile.getId());
        log.info("Viewing link: {}", uploadedFile.getWebViewLink());

        return uploadedFile.getId();
    }
}