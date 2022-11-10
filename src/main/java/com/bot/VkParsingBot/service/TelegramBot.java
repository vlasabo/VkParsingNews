package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.BotConfig;
import com.bot.VkParsingBot.config.BotStatus;
import com.bot.VkParsingBot.model.*;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private static final String STATUS_FOR_USER =
            "Вводите отслеживаемые слова, они автоматически запишутся. " +
                    "После каждого набора слов, который вы хотите отслеживать целиком отправляйте сообщение. \n" +
                    "Например если вы хотите получать оповещения о постах с днём рождения - введите \"День Рождения\" и отправьте сообщение\n" +
                    "В этом случае вы не получите оповещение о посте \"Сегодня был хороший ДЕНЬ\"\n" +
                    "Если же ввести \"День\" и отправить, затем \"Рождения\" и снова отправить - получите.\n\n" +
                    "Для окончания режима записи испрользуйте команду /stop_adding";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeywordsCollector keywordsCollector;
    private final BotConfig botConfig;

    @Getter
    private static HashMap<Long, BotStatus> userStatus;

    @Getter
    private static HashMap<Long, List<String>> wordsForAdding;

    @Getter
    @Setter
    private static HashMap<Long, List<String>> sentNews;

    @Autowired
    VkUser vkUser;

    @Autowired
    public TelegramBot(BotConfig botConfig) throws TelegramApiException {
        this.botConfig = botConfig;
        userStatus = new HashMap<>();
        wordsForAdding = new HashMap<>();
        sentNews = new HashMap<>();
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "регистрация"));
        commands.add(new BotCommand("/show_words", "показать отслеживаемые слова"));
        commands.add(new BotCommand("/add_words", "добавить отслеживаемые слова"));
        commands.add(new BotCommand("/stop_adding", "остановить добавление слов"));
        commands.add(new BotCommand("/delete_all_words", "очистить список отслеживаемых слов"));
        commands.add(new BotCommand("/check_news", "проверить что там в новостях по отслеживаемому"));
        execute(new SetMyCommands(commands, new BotCommandScopeDefault(), "ru"));
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
            if (getUserBotStatus(chatId) == BotStatus.NORMAL) {
                switch (messageText) {
                    case "/start":
                        newBotUser(update);
                        setUserBotStatus(chatId, BotStatus.REGISTRATION_ATTEMP);
                        break;
                    case "/show_words":
                        sendMessage(keywordsCollector.usersWord(chatId).toString(), chatId);
                        break;
                    case "/add_words":
                        sendMessage(STATUS_FOR_USER, chatId);
                        setUserBotStatus(chatId, BotStatus.WAITING);
                        break;
                    case "/check_news":
                        if (userRepository.findById(chatId).isPresent()) {
                            if (checkUserNews(chatId) == 0) {
                                sendMessage("Новостей не нашлось", chatId);
                            }
                        } else {
                            sendMessage("Сначала разрешите доступ к новостям и друзьям. Команда /start", chatId);
                        }
                        break;
                    case "/delete_all_words":
                        keywordsCollector.clearWords(chatId);
                        sendMessage("Все слова очищены", chatId);
                }
            } else if (getUserBotStatus(chatId) == BotStatus.WAITING) {
                switch (messageText) {
                    case "/stop_adding":
                        setUserBotStatus(chatId, BotStatus.NORMAL);
                        var resultWordsListForUser = TelegramBot.getWordsForAdding().get(chatId);
                        if (resultWordsListForUser.size() > 0) {
                            sendMessage("Отслеживаемые слова: "
                                    + keywordsCollector.addUsersWord(chatId, resultWordsListForUser), chatId);
                            TelegramBot.getWordsForAdding().get(chatId).clear();
                        }
                        break;
                    default:
                        List<String> userWordList;
                        if (TelegramBot.getWordsForAdding().containsKey(chatId)) {
                            userWordList = TelegramBot.getWordsForAdding().get(chatId);
                        } else {
                            userWordList = new ArrayList<>();
                        }
                        userWordList.add(messageText.toLowerCase().replace("\n", " "));
                        TelegramBot.getWordsForAdding().put(chatId, userWordList);
                }

            } else if (getUserBotStatus(chatId) == BotStatus.REGISTRATION_ATTEMP) {
                try {
                    registerVkUser(chatId, messageText);
                } catch (ClientException | ApiException e) {
                    log.error(e.getMessage());
                }
                setUserBotStatus(chatId, BotStatus.NORMAL);
            }

        }
    }

    public int checkUserNews(Long chatId) {
        Optional<User> userOpt = userRepository.findById(chatId);
        List<String> answerList;
        String text;
        int replySize = 0;

        if (userOpt.isPresent()) {
            if (!userOpt.get().getToken().isBlank()) {
                try {
                    answerList = vkUser.checkNewsVk(userOpt.get().getToken(), userOpt.get().getVkId(), chatId);
                    var sentNewsToUserList = TelegramBot.getSentNews()
                            .getOrDefault(chatId, Collections.emptyList());
                    var resultListToSend = answerList.stream()
                            .filter(s -> !sentNewsToUserList.contains(s)).collect(Collectors.toList());
                    resultListToSend.forEach(s -> sendMessage(s, chatId));
                    replySize = resultListToSend.size();
                    var hashMapSent = TelegramBot.getSentNews();
                    resultListToSend.addAll(sentNewsToUserList);
                    hashMapSent.put(chatId, resultListToSend);
                    TelegramBot.setSentNews(hashMapSent);
                } catch (ClientException | ApiException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return replySize;
    }

    private void registerVkUser(Long chatId, String messageText) throws ClientException, ApiException {
        if (messageText.contains("https://oauth.vk.com/blank.html#code=")) {
            String code = messageText.replace("https://oauth.vk.com/blank.html#code=", "");
            Optional<User> userOpt = userRepository.findById(chatId);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                user.setCode(code);
                var secretMap = vkUser.createAndSendTokenAndVkId(code);
                user.setToken(secretMap.entrySet().iterator().next().getValue());
                user.setVkId((secretMap.keySet().iterator().next()));
                userRepository.save(user);
                sendMessage("Вы зарегистрировали персональный ключ", chatId);
            }
        } else {
            sendMessage("строка должна начинаться с https://oauth.vk.com/blank.html#code=\n " +
                    "Для повторной попытки снова введите команду /start", chatId);
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
        }
        text = "Вы успешно зарегистрированы\nДля доступа бота к новостям откройте ссылку " +
                "https://oauth.vk.com/authorize?client_id=51465704&display=page&redirect_uri=" +
                "https://oauth.vk.com/blank.html&scope=wall,friends&response_type=code&v=5.131 " +
                "и разрешите доступ ТОЛЬКО к друзьям и стене, затем пришлите адрес страницы, на которую вас переадресует\n" +
                "Это никак не повлияет на безопасность вашего аккаунта";
        sendMessage(text, userId);

    }

    private void sendMessage(String text, Long chatId) {
        if (text.length() > 4000) {
            String fullText = text;
            text = text.substring(fullText.length() - 4000);
            sendMessage(fullText.substring(0, fullText.length() - 4000), chatId);
        }
        SendMessage sm = new SendMessage();
        sm.setText(text);
        sm.setChatId(chatId);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        log.warn(text.substring(101));
    }

    private static BotStatus getUserBotStatus(Long chatId) {
        HashMap<Long, BotStatus> userSettings = TelegramBot.getUserStatus();
        BotStatus settings = userSettings.get(chatId);
        if (settings == null) {
            return BotStatus.NORMAL;
        }
        return settings;
    }

    private static void setUserBotStatus(Long chatId, BotStatus botStatus) {
        HashMap<Long, BotStatus> userSettings = TelegramBot.getUserStatus();
        userSettings.put(chatId, botStatus);
    }

}
