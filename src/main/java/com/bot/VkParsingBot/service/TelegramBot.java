package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.BotConfig;
import com.bot.VkParsingBot.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeywordsCollector keywordsCollector;
    private final BotConfig botConfig;

    @Autowired
    public TelegramBot(BotConfig botConfig) throws TelegramApiException {
        this.botConfig = botConfig;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "регистрация"));
        commands.add(new BotCommand("/show_words", "показать отслеживаемые слова"));
        execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
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
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            switch (messageText) {
                case "/start":
                    newBotUser(update);
                    break;
                case "/show_words":
                    sendMessage(keywordsCollector.usersWord(chatId).toString(), chatId);
            }

        }
    }

    private void newBotUser(Update update) {
        String text;
        Long userId = update.getMessage().getChatId();
        if (userRepository.findById(userId).isEmpty()) {
            Chat chat = update.getMessage().getChat();
            User user = new User();
            user.setId(userId);
            user.setUserName(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegistrationDate(Timestamp.valueOf(LocalDateTime.now()));
            userRepository.save(user);
            text = "Вы успешно зарегистрированы";
        } else {
            text = "Вы уже зарегистрированы";
        }
        sendMessage(text, userId);

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
