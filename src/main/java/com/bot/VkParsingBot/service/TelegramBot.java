package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.BotProperties;
import com.bot.VkParsingBot.model.BotStatus;
import com.bot.VkParsingBot.model.User;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.bot.VkParsingBot.model.BotStatus.WAITING;

@Service
@EnableConfigurationProperties(value = BotProperties.class)
public class TelegramBot extends TelegramLongPollingBot {

    private static final String STATUS_FOR_USER =
            "Вводите отслеживаемые слова, они автоматически запишутся. " +
                    "После каждого набора слов, который вы хотите отслеживать целиком отправляйте сообщение. \n" +
                    "Например если вы хотите получать оповещения о постах с днём рождения - введите \"День Рождения\" и отправьте сообщение\n" +
                    "В этом случае вы не получите оповещение о посте \"Сегодня был хороший ДЕНЬ\"\n" +
                    "Если же ввести \"День\" и отправить, затем \"Рождения\" и снова отправить - получите.\n\n" +
                    "Для окончания режима записи испрользуйте команду /stop_adding";

    private final UserService userService;
    // TODO: 11.12.2022 советую группировать поля. Сервисы с сервисами, проперти с пропертями.
    //  У тебя все в кучу сейчас
    private final BotProperties botProperties;
    // TODO: 11.12.2022 геттер над полем класса - признак, что ошиблись с поректированием.
    //  Советую вынести коллекции из рантайма в отдельный класс. И написать методы для работы с ними.
    //  Будет проще менеджить и ограничивать использование с развитием проекта.
    //  По сути, сейчас ты нарушаешь инкапсуляцию
    @Getter
    private static Map<Long, BotStatus> userStatus;
    //добавляемое количество слов не будет большим, порядок не важен, одинаковые слова не нужны,
    // так что эта реализация вместо CopyOnWriteArrayList
    @Getter
    // TODO: 11.12.2022 кст, зачем статик? у тебя синглтон, как бы)
    //  CopyOnWriteArraySet -> Set. А инициализируй чем угодно. Кст, посмотри в сторону ConcurrentHashMap.newKeySet()
    private static Map<Long, CopyOnWriteArraySet<String>> wordsForAdding;
    private final VkUser vkUser;
    private final VkService vkService;
    private final KeywordsCollector keywordsCollector;

    @Autowired
    public TelegramBot(UserService userService, BotProperties botProperties,
                       VkUser vkUser, VkService vkService, KeywordsCollector keywordsCollector) throws TelegramApiException {
        this.userService = userService;
        this.botProperties = botProperties;
        this.vkUser = vkUser;
        this.vkService = vkService;
        this.keywordsCollector = keywordsCollector;
        userStatus = new ConcurrentHashMap<>();
        wordsForAdding = new ConcurrentHashMap<>();

        // TODO: 11.12.2022 кажется, это претендент на какой-нить PostConstruct
        execute(new SetMyCommands(getBotCommands(), new BotCommandScopeDefault(), "ru"));
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    // TODO: 11.12.2022 стоит дальше декомпозировать, вынеся часть кода в приватные методы
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            switch (getUserBotStatus(chatId)) {
                case NORMAL:
                    switch (messageText) {
                        // TODO: 11.12.2022 Мб стоит вынести класс-констант, содержащий команды?
                        case "/start":
                            newBotUser(update);
                            setUserBotStatus(chatId, BotStatus.REGISTRATION_ATTEMPT);
                            break;
                        case "/show_words":
                            if (userService.findById(chatId).isPresent()) {
                                sendMessage(userService.findById(chatId).get().getUserWordsList().toString(), chatId);
                            } else {
                                sendMessage("сначала зарегистрируйтесь", chatId);
                            }
                            break;
                        case "/add_words":
                            sendMessage(STATUS_FOR_USER, chatId);
                            setUserBotStatus(chatId, WAITING);
                            break;
                        case "/check_news":
                            if (userService.findById(chatId).isPresent()) {
                                if (checkUserNews(chatId) == 0) {
                                    sendMessage("Новостей не нашлось", chatId);
                                }
                            } else {
                                sendMessage("Сначала разрешите доступ к новостям и друзьям. Команда /start", chatId);
                            }
                            break;
                        case "/delete_all_words":
                            if (userService.findById(chatId).isPresent()) {
                                User user = userService.findById(chatId).get();
                                user.getUserWordsList().clear();
                                userService.save(user);
                                sendMessage("Все слова очищены", chatId);
                            } else {
                                sendMessage("сначала зарегистрируйтесь", chatId);
                            }
                    }
                    break;
                case WAITING:
                    if ("/stop_adding".equals(messageText)) {
                        setUserBotStatus(chatId, BotStatus.NORMAL);
                        var resultWordsListForUser = TelegramBot.getWordsForAdding().get(chatId);
                        if (resultWordsListForUser.size() > 0) {// TODO: 11.12.2022 советую оставлять пустую строку перед условной конструкцией/циклом
                            writeWordsForUser(chatId, resultWordsListForUser);
                        }
                    } else {
                        addWordsForUser(chatId, messageText);
                    }
                    break;
                case REGISTRATION_ATTEMPT:
//TODO: а вот тут пустая строка кажется лишней. Я бы в целом советовал содержимое case в приватные методы вынести
                    try {
                        registerVkUser(chatId, messageText);
                    } catch (ClientException | ApiException e) {
                        System.out.println(e.getMessage());
                    }
                    setUserBotStatus(chatId, BotStatus.NORMAL);
                    break;
                // TODO: 11.12.2022 никогда не забывай про default. Если кажется, что не нужен - кидай в нем эксепшн
            }
        }
    }

    private void addWordsForUser(Long chatId, String messageText) {
        CopyOnWriteArraySet<String> userWordList;

        // TODO: 11.12.2022 несколько корявый if-else. Я бы сделал так:
//        TelegramBot.getWordsForAdding().putIfAbsent(chatId, new CopyOnWriteArraySet<>());
//        var userWordList = TelegramBot.getWordsForAdding().get(chatId);

        if (TelegramBot.getWordsForAdding().containsKey(chatId)) {
            userWordList = TelegramBot.getWordsForAdding().get(chatId);
        } else {
            userWordList = new CopyOnWriteArraySet<>();
        }
        userWordList.add(messageText.toLowerCase().replace("\n", " "));
        TelegramBot.getWordsForAdding().put(chatId, userWordList);
    }

    private void writeWordsForUser(Long chatId, CopyOnWriteArraySet<String> resultWordsListForUser) {
        // TODO: 11.12.2022 Зачем возвращать опшнал, если не процессишь его в функцциональном стиле? Это избыточно
        if (userService.findById(chatId).isEmpty()) {
            sendMessage("Вы не зарегистрированы", chatId);
            TelegramBot.getWordsForAdding().get(chatId).clear();
            return;
        }
        sendMessage("Отслеживаемые слова: "
                + keywordsCollector.addUsersWord(userService.findById(chatId).get(), resultWordsListForUser), chatId);
        sendMessage("можете проверить новости командой /check_news " +
                "или подождать пока я сделаю это за вас и пришлю вам ссылки", chatId);
        TelegramBot.getWordsForAdding().get(chatId).clear();// TODO: 11.12.2022 не хватает пустой строки перед этой
    }//TODO: ниже лишняя пустая строка


    public int checkUserNews(Long chatId) {// TODO: 11.12.2022 не самый красивый метод .Стоит подумать, как переписать
        Optional<User> userOpt = userService.findById(chatId);
        int replySize = 0;

        if (userOpt.isPresent()) {
            if (userOpt.get().getToken() == null) { //юзер зарегистрировался но токен от ВК не дал
                return 0;
            }
            if (!userOpt.get().getToken().isBlank()) {
                try {
                    User user = userOpt.get();
                    var answerAndSaveMap = vkService
                            .getNewsToSendAndSave(user);
                    var answerList = answerAndSaveMap.getOrDefault("sending", new ArrayList<>());
                    // TODO: 11.12.2022 как думаешь, что будет, если вызвать forEach над пустым листом?)
                    if (answerList.size() > 0) {
                        answerList.forEach(s -> sendMessage(s, chatId));
                        answerAndSaveMap.getOrDefault("saving", new ArrayList<>())
                                .forEach(sent -> user.getSentNews().add(sent));
                        userService.save(user);
                    }
                    replySize = answerList.size();
                } catch (ClientException | ApiException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return replySize;
    }

    // TODO: 11.12.2022 Посмотри, как обрабатывать Optional в функциональном стиле. Будет лаконичнее и красивее
    private void registerVkUser(Long chatId, String messageText) throws ClientException, ApiException {
        if (messageText.contains("https://oauth.vk.com/blank.html#code=")) {
            String code = messageText.replace("https://oauth.vk.com/blank.html#code=", "");
            Optional<User> userOpt = userService.findById(chatId);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                user.setCode(code);
                var secretMap = vkUser.createAndSendTokenAndVkId(code);
                user.setToken(secretMap.entrySet().iterator().next().getValue());
                user.setVkId((secretMap.keySet().iterator().next()));
                userService.save(user);
                sendMessage("Вы зарегистрировали персональный ключ. " +
                        "Начните добавлять слова командой /add_words", chatId);
            }
        } else {
            sendMessage("строка должна начинаться с https://oauth.vk.com/blank.html#code=\n " +
                    "Для повторной попытки снова введите команду /start", chatId);
        }
    }

    // TODO: 11.12.2022 Посмотри, как обрабатывать Optional в функциональном стиле. Будет лаконичнее и красивее
    private void newBotUser(Update update) {
        String text;
        Long userId = update.getMessage().getChatId();
        if (userService.findById(userId).isEmpty()) {
//            TODO: это чудо претендует на вынесение в отдельный метод
            Chat chat = update.getMessage().getChat();
            User user = new User();
            user.setId(userId);
            user.setUserName(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegistrationDate(LocalDateTime.now());
            userService.save(user);
        }
        text = "Вы успешно зарегистрированы\nДля доступа бота к новостям откройте ссылку " +
                "https://oauth.vk.com/authorize?client_id=51465704&display=page&redirect_uri=" +
                "https://oauth.vk.com/blank.html&scope=wall,friends,offline&response_type=code&v=5.131 " +
                "и разрешите доступ ТОЛЬКО к друзьям и стене, затем пришлите адрес страницы, на которую вас переадресует\n" +
                "Это никак не повлияет на безопасность вашего аккаунта";
        sendMessage(text, userId);

    }

    // TODO: 11.12.2022 кажется, этот метод вообще должен быть в отдельном классе)
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
    }

    // TODO: 11.12.2022 статик методов в сервисных классах быть не должно
    private static BotStatus getUserBotStatus(Long chatId) {
        Map<Long, BotStatus> userSettings = TelegramBot.getUserStatus();
        BotStatus settings = userSettings.get(chatId);
        if (settings == null) {
            return BotStatus.NORMAL;
        }
        return settings;
    }

    private static void setUserBotStatus(Long chatId, BotStatus botStatus) {
        Map<Long, BotStatus> userSettings = TelegramBot.getUserStatus();
        userSettings.put(chatId, botStatus);
    }

    private List<BotCommand> getBotCommands() {
        List<BotCommand> commands = new ArrayList<>();
        // TODO: 11.12.2022 команды точно стоит вынести в константы. А лучше вообще в enum
        commands.add(new BotCommand("/start", "регистрация"));
        commands.add(new BotCommand("/show_words", "показать отслеживаемые слова"));
        commands.add(new BotCommand("/add_words", "добавить отслеживаемые слова"));
        commands.add(new BotCommand("/stop_adding", "остановить добавление слов"));
        commands.add(new BotCommand("/delete_all_words", "очистить список отслеживаемых слов"));
        commands.add(new BotCommand("/check_news", "проверить что там в новостях по отслеживаемому"));
        return commands;
    }
}
