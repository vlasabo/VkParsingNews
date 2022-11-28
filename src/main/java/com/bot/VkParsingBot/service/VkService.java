package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.Sent;
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
import java.util.List;
import java.util.stream.Collectors;

//TODO: доразобрать checkNewsVk
@Service
@RequiredArgsConstructor
public class VkService {

    private static final Integer NEWS_COUNT = 100;
    private final SentService sentService;
    private final KeywordsCollector keywordsCollector;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<String> checkNewsVk(String token, Integer vkId, Long userId) throws ClientException, ApiException {
        var answerList = new ArrayList<String>();
        var userWordsList = keywordsCollector.usersWord(userId);
        if (userWordsList.size() == 0) { //если отслеживаемых слов нет - вернем пустой лист
            return answerList;
        }
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = new UserActor(vkId, token);

        var filterList = getFilters();
        var listNews = getNews(vk, actor, filterList);
       /*  StringBuilder sb = new StringBuilder();

       for (var item : listNews) { //TODO: попробовать переделать на стрим
            StringBuilder source = new StringBuilder();
            source.append(item.getRaw().get("source_id")).append("_").append(item.getRaw().get("post_id"));
            String sentNewsData = source.toString();
            if (sentService.checkSentNewsForUser(userId, sentNewsData)) { //если новость уже отправляли - следующая новость
                continue;
            }

            String dateString = getDateAsString(item);
            String news = item.getRaw().get("text").toString().toLowerCase();
            if (news.isBlank()) { //если текста нет - следующая новость
                source.setLength(0);
                continue;
            }
            String resultNews = news.toLowerCase().replaceAll("[^A-Za-zА-Яа-я0-9 ]", " ");
            var contain = userWordsList.stream().anyMatch(x -> resultNews.contains(" " + x.toLowerCase() + " "));
            if (contain) {
                answerList.add(sb.append(dateString)
                        .append("\n\n")
                        .append("https://vk.com/feed?w=wall")
                        .append(source).append("\n")
                        .append(item.getRaw().get("text").toString().replace("\\n", "\n"))
                        .append("\n\n").toString());
                sb.setLength(0);
                source.setLength(0);
                Sent sent = new Sent();
                sent.setSentNewsData(sentNewsData);
                sent.setUserId(userId);
                sentService.save(sent);
            }
            source.setLength(0);
        }*/

        return listNews.stream()
                .filter(item -> sentService.checkSentNewsForUser(userId, getNewsSource(item))) //нет в отправленных
                .filter(item -> !item.getRaw().get("text").toString().isBlank()) //не пустой текст
                .filter(item -> checkContainsUserWords(item, userWordsList)) //содержит отслеживаемые слова
                .peek(item -> //TODO: добавлять в отправленные после фактической отправки, а не добавления в список к отправке
                        sentService.save(new Sent(getNewsSource(item), userId))) //добавили в отправленные
                .map(this::getResultNewsToAdd) //в строку
                .collect(Collectors.toList());

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
                .startTime(beginningOfTodayInSec - 86400 * 8) //TODO  вернуть один день
                .execute()
                .getItems();
    }


    private List<Filters> getFilters() {
        return List.of(Filters.POST);
    }
}
