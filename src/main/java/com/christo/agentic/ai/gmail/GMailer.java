package com.christo.agentic.ai.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.google.api.services.gmail.model.Profile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
@RequiredArgsConstructor
@Service
public class GMailer {
    
    private final DateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private final Gmail gmail;

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
            return simpleDateFormat.parse(date);
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
        Message message = createMessageWithEmail(recipientId, getUserEmail(), subject, messageText);
        Message sendMessage = gmail.users().messages().send("me", message).execute();
        log.info("Message sent: {}", sendMessage.getId());
        return String.format("Message successfully sent with id: %s}", sendMessage.getId());
    }

    @SneakyThrows
    private String getUserEmail() {
        Profile profile = gmail.users().getProfile("me").execute();
        return profile.getEmailAddress();
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
