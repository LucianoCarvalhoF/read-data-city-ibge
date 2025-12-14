package com.ifpb.read_data_city_ibge.controllers;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.ifpb.read_data_city_ibge.services.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/drive-auth")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class DriveAuthController {
    @Value("${google.oauth.redirect.uri}")
    private String redirectUri;

    private final GoogleDriveService driveService;

    @GetMapping("/authorize")
    public RedirectView startAuthorization() {
        GoogleAuthorizationCodeFlow flow = driveService.getFlow();
        AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();

        String url = authorizationUrl
                .setRedirectUri(redirectUri)
                .setState(GoogleDriveService.SERVICE_USER_ID)
                .build();

        return new RedirectView(url);
    }

    @GetMapping("/callback")
    @SuppressWarnings("rawtypes")
    public ResponseEntity handleCallback(@RequestParam(required = false) String code,
                                         @RequestParam(required = false) String error,
                                         @RequestParam(required = false) String state) {
        if (Objects.nonNull(error)) {
            log.warn("Authorization denied by the user: {}", error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (Objects.isNull(code)) {
            log.warn("Authorization code missing.");
            return ResponseEntity.badRequest().build();
        }

        try {
            GoogleAuthorizationCodeFlow flow = driveService.getFlow();
            String userId = GoogleDriveService.SERVICE_USER_ID;

            TokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();

            Credential credential = flow.createAndStoreCredential(tokenResponse, userId);
            driveService.setDriveClient(credential);
            log.info("The Google Drive Service client is initialized and ready to use.");

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.warn("Error processing callback and obtaining tokens: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}