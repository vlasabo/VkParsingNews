package com.bot.VkParsingBot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String token;
}
