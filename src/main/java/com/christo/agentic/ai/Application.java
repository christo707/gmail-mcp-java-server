package com.christo.agentic.ai;

import com.christo.agentic.ai.gmail.GMailer;
import lombok.SneakyThrows;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.christo.agentic.ai")
@EnableAutoConfiguration
public class Application {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ToolCallbackProvider gmailTools(GMailer gmailer) {
		return  MethodToolCallbackProvider.builder().toolObjects(gmailer).build();
	}

}
