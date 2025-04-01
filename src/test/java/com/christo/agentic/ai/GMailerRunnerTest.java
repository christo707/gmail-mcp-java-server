package com.christo.agentic.ai;

import com.christo.agentic.ai.gmail.GMailer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;


class GMailerRunnerTest {

    private static GMailer gMailer;

//    static  {
//        String tokensDirectoryPath = "src/test/resources/tokens";
//        String credentialsPath = "/credentials.json";
//        GmailConfig gmail = new GmailConfig();
//        gmail.setCredentialsPath(credentialsPath);
//        gmail.setTokensPath(tokensDirectoryPath);
//        gMailer = new GMailer(gmail);
//    }


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
