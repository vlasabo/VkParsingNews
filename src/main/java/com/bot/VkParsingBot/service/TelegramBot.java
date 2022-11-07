package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.BotConfig;
import com.bot.VkParsingBot.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    private final BotConfig botConfig;

    @Autowired
    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            switch (messageText){
                case "/start":
                    newBotUser(update);
                    break;
            }

        }
    }

    private void newBotUser(Update update) {
        sendMessage("test", update.getMessage().getChatId());
        Chat chat = update.getMessage().getChat();
        User user = new User();
        user.setId(chat.getId());
        user.setUserName(chat.getUserName());
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now()));
        userRepository.save(user);
    }

    private void sendMessage(String text, Long chatId) {
        SendMessage sm = new SendMessage();
        sm.setText(text);
        sm.setChatId(chatId);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
