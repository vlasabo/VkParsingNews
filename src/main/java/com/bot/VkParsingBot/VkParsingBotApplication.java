package com.bot.VkParsingBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VkParsingBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(VkParsingBotApplication.class, args);
	}

}
