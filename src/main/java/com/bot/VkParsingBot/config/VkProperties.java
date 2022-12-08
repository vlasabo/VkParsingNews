package com.bot.VkParsingBot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class VkProperties {
    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private Integer id;
}
