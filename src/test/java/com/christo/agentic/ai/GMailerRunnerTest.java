package com.christo.agentic.ai;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;


class GMailerRunnerTest {

    private final String tokensDirectoryPath = "src/test/resources/tokens";

    private final String credentialsPath = "/credentials.json";

    private final GMailer gMailer = new GMailer(credentialsPath, tokensDirectoryPath);

    @Test
    @SneakyThrows
    void testGetLabels() {
        System.out.println(gMailer.getSelfLabels());
    }

    @Test
    @SneakyThrows
    void getUnreadMessages() {
        System.out.println(gMailer.getUnreadMessages());
    }

    @Test
    @SneakyThrows
    void testReadMessage() {
        System.out.println(gMailer.readEmail("195eba182adc829c"));
    }

    @Test
    @SneakyThrows
    void testGetEmailAddress() {
        System.out.println(gMailer.getUserEmail());
    }

    @Test
    @SneakyThrows
    void testTrashMessage() {
        System.out.println(gMailer.trashEmail("195eba182adc829c"));
    }

    @Test
    @SneakyThrows
    void testmarkEmailAsRead() {
        System.out.println(gMailer.markEmailAsRead("195eba182adc829c"));
    }

    @Test
    @SneakyThrows
    void testSendEmail() {
        System.out.println(gMailer.sendEmail("christopherrozario7@gmail.com",
                "Test Email",
                "This is a test email"));
    }
}
