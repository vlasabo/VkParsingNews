package com.bot.VkParsingBot.config;

import com.bot.VkParsingBot.service.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
//TODO: я бы вынес этот класс из пакета конфигурации. Вполне можно засунуть его в пакет init, например
//TODO: Общее замечание: не рекомендую инжектить в поля. ЛУчше пользоваться инжектом в конструктор
//https://www.baeldung.com/spring-injection-lombok
public class BotInitializer {

    @Autowired
    TelegramBot bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
