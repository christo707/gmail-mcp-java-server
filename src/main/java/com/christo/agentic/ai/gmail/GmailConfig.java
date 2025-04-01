package com.christo.agentic.ai.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Configuration
public class GmailConfig {

    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private static final String APP_NAME = "GmailMCPMailer";

    private static final List<String> scopes = Collections.singletonList(GmailScopes.GMAIL_MODIFY);

    private static final String CREDENTIALS_PATH = "/credentials.json";

    private static final String TOKENS_PATH = "src/test/resources/tokens";

    @SneakyThrows
    @Bean
    public Gmail getGmail() {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new com.google.api.services.gmail.Gmail.Builder(httpTransport, jsonFactory,
                getCredentials(httpTransport)).setApplicationName(APP_NAME).build();
    }

    @SneakyThrows
    private Credential getCredentials(final NetHttpTransport httpTransport) {

        InputStream in = GMailer.class.getResourceAsStream(CREDENTIALS_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                clientSecrets, scopes).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_PATH)))
                .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
