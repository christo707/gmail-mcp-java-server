package com.christo.agentic.ai;

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
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.google.api.services.gmail.model.Profile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class GMailer {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String APPLICATION_NAME = "GmailMCPMailer";

    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);

    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private final String credentialsPath;

    private final String tokensPath;

    private final String userEmail;

    private final Gmail gmail;


    GMailer(final String credentialsPath, final String tokensPath) {
        this.credentialsPath = credentialsPath;
        this.tokensPath = tokensPath;
        this.gmail = getGmailService();
        this.userEmail = getUserEmail();
    }

    @SneakyThrows
    public String getUserEmail() {
        Profile profile = gmail.users().getProfile("me").execute();
        return profile.getEmailAddress();
    }

    @SneakyThrows
    private Gmail getGmailService() {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport)).setApplicationName(APPLICATION_NAME).build();
    }

    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {

        InputStream in = GMailer.class.getResourceAsStream(credentialsPath);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + credentialsPath);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensPath))).setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @SneakyThrows
    public List<String> getSelfLabels() {
        String user = "me";
        ListLabelsResponse listResponse = gmail.users().labels().list(user).execute();
        return listResponse.getLabels().stream().map(Label::getName).toList();
    }

    public List<Message> getUnreadMessages() throws IOException {
        String query = "in:inbox is:unread category:primary";
        ListMessagesResponse response = gmail.users().messages().list("me").setQ(query).execute();
        List<Message> result = new ArrayList<>();
        while (!response.getMessages().isEmpty()) {
            result.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = gmail.users().messages().list("me").setQ(query).setPageToken(pageToken).execute();
            } else {
                break;
            }
        }
        return result;
    }

    @SneakyThrows
    public Email readEmail(String messageId) {
        Message message = gmail.users().messages().get("me", messageId).execute();
        return Email.builder()
                .from(message.getPayload().getHeaders().stream()
                        .filter(header -> header.getName().equals("From")).findFirst()
                        .map(MessagePartHeader::getValue).orElse(null))
                .to(message.getPayload().getHeaders().stream()
                        .filter(header -> header.getName().equals("To")).findFirst()
                        .map(MessagePartHeader::getValue).orElse(null))
                .date(message.getPayload().getHeaders().stream()
                        .filter(header -> header.getName().equals("Date")).findFirst()
                        .map(MessagePartHeader::getValue).map(this::parseDate).orElse(null))
                .subject(message.getPayload().getHeaders().stream()
                        .filter(header -> header.getName().equals("Subject")).findFirst()
                        .map(MessagePartHeader::getValue).orElse(null))
                .body(getBody(message)).build();
    }

    private Date parseDate(String date) {
        try {
            return df.parse(date);
        } catch (ParseException e) {
            log.error("Error parsing date", e);
            return null;
        }
    }

    private String getBody(Message message) {
        return Optional.ofNullable(message.getPayload().getParts())
                .flatMap(parts -> parts.stream()
                        .filter(part -> part.getMimeType().equals("text/plain"))
                        .findFirst()
                        .map(part -> new String(part.getBody().decodeData())))
                .orElse(null);
    }

    @SneakyThrows
    public String trashEmail(String emailId) {
        gmail.users().messages().trash("me", emailId).execute();
        log.info("Email moved to trash: {}", emailId);
        return String.format("Email with id %s moved to trash successfully.", emailId);
    }

    @SneakyThrows
    public String markEmailAsRead(String emailId) {
        ModifyMessageRequest mods = new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList("UNREAD"));
        gmail.users().messages().modify("me", emailId, mods).execute();
        log.info("Email marked as read: {}", emailId);
        return String.format("Email with id %s marked as read.", emailId);
    }

    @SneakyThrows
    public String sendEmail(String recipientId, String subject, String messageText) {
        Message message = createMessageWithEmail(recipientId, userEmail, subject, messageText);
        Message sendMessage = gmail.users().messages().send("me", message).execute();
        log.info("Message sent: {}", sendMessage.getId());
        return String.format("{\"status\": \"success\", \"message_id\": \"%s\"}", sendMessage.getId());
    }

    @SneakyThrows
    private Message createMessageWithEmail(String to, String from, String subject, String bodyText) {
        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
