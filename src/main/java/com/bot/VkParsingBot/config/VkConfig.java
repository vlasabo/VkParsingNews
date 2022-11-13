package com.bot.VkParsingBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

//TODO: весь набор замечаний из BotConfig.
// По сути, именно конфигурационных классов у тебя и нет.
// Можно сделать и вынести туда какой-нибудь @EnableScheduling заодно
@Configuration
@Data
@PropertySource("application.properties")
public class VkConfig {
    @Value("${app.code}")
    private String code;
    @Value("${app.id}")
    private Integer id;
}
