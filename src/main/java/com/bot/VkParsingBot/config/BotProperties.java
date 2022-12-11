package com.bot.VkParsingBot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    // TODO: 11.12.2022 просто над класс навесь аннотации для геттеров и сеттеров, над полями избыточно.
    //  Во втором проперти-классе тоже
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String token;
}
