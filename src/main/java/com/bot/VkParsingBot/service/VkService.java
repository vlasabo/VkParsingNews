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

//TODO: доразобрать checkNewsVk
@Service
@RequiredArgsConstructor
public class VkService {

    private static final Integer NEWS_COUNT = 100;
    private final VkUser vkUser;
    private final SentService sentService;
    private final KeywordsCollector keywordsCollector;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<String> checkNewsVk(String token, Integer vkId, Long userId) throws ClientException, ApiException {
        var answerList = new ArrayList<String>();
        var userWordsList = keywordsCollector.usersWord(userId);
        if (userWordsList.size() == 0) {
            return answerList;
        }
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = vkUser.createActorFromToken(token, vkId); //TODO: вынести получение пользователя в отдельный класс
        var filterList = getFilters();
        var listNews = getNews(vk, actor, filterList);
        StringBuilder sb = new StringBuilder();

        for (var item : listNews) { //TODO: попробовать переделать на стрим
            StringBuilder source = new StringBuilder();
            source.append(item.getRaw().get("source_id")).append("_").append(item.getRaw().get("post_id"));
            String sentNewsData = source.toString();
            if (sentService.checkSentNewsForUser(userId, sentNewsData)) {
                continue;
            }
            String dateString = getDateAsString(item);
            for (var es : item.getRaw().entrySet()) {
                if (es.getKey().equals("text")) { //TODO: проверить везде ли есть поле текст  при фильтре
                    String news = es.getValue().toString().toLowerCase();
                    if (news.isBlank()) {
                        dateString = "";
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
                                .append(es.getValue().toString().replace("\\n", "\n"))
                                .append("\n\n").toString());
                        sb.setLength(0);
                        source.setLength(0);
                        Sent sent = new Sent(); //TODO: добавлять в отправленные после фактической отправки, а не добавления в список К отправке
                        sent.setSentNewsData(sentNewsData);
                        sent.setUserId(userId);
                        sentService.save(sent);
                    }
                    dateString = "";
                    source.setLength(0);
                }

            }
        }
        return answerList;
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
