package com.bot.VkParsingBot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BotStatus {
    NORMAL("Ожидание команд из списка"),
    WAITING("Ожидается ввод пользовательских слов"),
    REGISTRATION_ATTEMPT("Ожидается ключ доступа к ВКонтакте пользователя");

    @Getter
    private final String title;
}
