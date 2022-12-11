package com.bot.VkParsingBot.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BotStatus {
    // TODO: 11.12.2022 Писать описание с маленькой буквы - не оч. И докинь им геттер
    NORMAL("ожидание команд из списка"),
    WAITING("ожидается ввод пользовательских слов"),
    REGISTRATION_ATTEMPT("ожидается ключ доступа к ВКонтакте пользователя");

    private final String title;
}
