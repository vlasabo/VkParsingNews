package com.bot.VkParsingBot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class VkParsingBotApplication {//TODO: Минорный момент. Пустая строка после последнего метода не нужна

	public static void main(String[] args) {
		SpringApplication.run(VkParsingBotApplication.class, args);
	}
}
