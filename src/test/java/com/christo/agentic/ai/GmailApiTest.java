package com.christo.agentic.ai;

import com.christo.agentic.ai.gmail.GMailer;
import lombok.SneakyThrows;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
		classes = {Application.class})
class GmailApiTest implements WithAssertions {

	@Autowired
	private GMailer gMailer;

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
		System.out.println(gMailer.markEmailAsRead("195eba17fd90b67c"));
	}

	@Test
	@SneakyThrows
	void testSendEmail() {
		System.out.println(gMailer.sendEmail("christopherrozario7@gmail.com",
				"Test Email",
				"This is a test email"));
	}



}
