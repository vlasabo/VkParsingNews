package com.bot.VkParsingBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//TODO: нужна ли здесь аннотация @Data? По-моему, нет. @Getter и @Setter хватит с головой
@Data
//TODO: не увидел файла application.properties.
// Однако в любом случае советую использовать application.yml. Он более читабельный
@PropertySource("application.properties")
//TODO: это не конфиг, а проперти. Советую ознакомиться с
// https://www.baeldung.com/spring-enable-config-properties
public class BotConfig {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String token;
}
