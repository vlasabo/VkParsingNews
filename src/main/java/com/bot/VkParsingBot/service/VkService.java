package com.bot.VkParsingBot.service;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.newsfeed.Filters;
import com.vk.api.sdk.oneofs.NewsfeedNewsfeedItemOneOf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VkService {

    private static final Integer NEWS_COUNT = 100;
    private final SentService sentService;
    private final KeywordsCollector keywordsCollector;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Map<String, List<String>> getNewsToSendAndSave(String token, Integer vkId, Long userId)
            throws ClientException, ApiException { //в мапе лежат лист сообщений к отправке и лист сообщений к сохранению
        var userWordsList = keywordsCollector.usersWord(userId);
        if (userWordsList.size() == 0) { //если отслеживаемых слов нет - вернем пустую мапу
            return new HashMap<>();
        }
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = new UserActor(vkId, token);

        var filterList = getFilters();
        var listNews = getNews(vk, actor, filterList);

        var resultListToSaveSent = new ArrayList<String>();
        var resultListToSending = listNews.stream()
                .filter(item -> sentService.checkSentNewsForUser(userId, getNewsSource(item))) //нет в отправленных
                .filter(item -> !item.getRaw().get("text").toString().isBlank()) //не пустой текст
                .filter(item -> checkContainsUserWords(item, userWordsList)) //содержит отслеживаемые слова
                .peek(item -> resultListToSaveSent.add(getNewsSource(item))) //добавили в лист "отправленные"
                .map(this::getResultNewsToAdd) //в строку
                .collect(Collectors.toList());
        Map<String, List<String>> resultMap = new HashMap<>();
        resultMap.put("sending", resultListToSending);
        resultMap.put("saving", resultListToSaveSent);
        return resultMap;
    }

    private String getNewsSource(NewsfeedNewsfeedItemOneOf item) {
        return item.getRaw().get("source_id").toString()
                .concat("_")
                .concat(item.getRaw().get("post_id").toString());
    }

    private boolean checkContainsUserWords(NewsfeedNewsfeedItemOneOf item, List<String> userWordsList) {
        return userWordsList.stream()
                .anyMatch(x ->
                        item.getRaw().get("text").toString().toLowerCase().replaceAll("[^A-Za-zА-Яа-я0-9 ]", " ")
                                .contains(" " + x.toLowerCase() + " "));
    }

    private String getResultNewsToAdd(NewsfeedNewsfeedItemOneOf item) {
        StringBuilder sb = new StringBuilder();
        return sb.append(getDateAsString(item))
                .append("\n\n")
                .append("https://vk.com/feed?w=wall")
                .append(getNewsSource(item))
                .append("\n")
                .append(item.getRaw().get("text").toString().replace("\\n", "\n"))
                .append("\n\n").toString();
    }

    private String getDateAsString(NewsfeedNewsfeedItemOneOf item) {
        long seconds = Long.parseLong(item.getRaw().get("date").toString());
        return LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.ofHours(3)).format(formatter);
    }

    private List<NewsfeedNewsfeedItemOneOf> getNews(VkApiClient vk, UserActor actor, List<Filters> filterList)
            throws ClientException, ApiException {
        int beginningOfTodayInSec = (int) LocalDateTime.now().with(LocalTime.MIN).toEpochSecond(ZoneOffset.ofHours(3));
        return vk.newsfeed()
                .get(actor)
                .filters(filterList)
                .returnBanned(false)
                .count(VkService.NEWS_COUNT)
                .startTime(beginningOfTodayInSec)
                .execute()
                .getItems();
    }


    private List<Filters> getFilters() {
        return List.of(Filters.POST);
    }
}
