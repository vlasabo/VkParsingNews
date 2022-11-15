package com.bot.VkParsingBot.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BotStatus {
    NORMAL("ожидание команд из списка"),
    WAITING("ожидается ввод пользовательских слов"),
    REGISTRATION_ATTEMP("ожидается ключ доступа к ВКонтакте пользователя");

    private final String title;
}
