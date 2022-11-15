package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.VkProperties;
import com.bot.VkParsingBot.model.User;
import com.bot.VkParsingBot.repository.UserRepository;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiAuthException;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.newsfeed.Filters;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
@EnableConfigurationProperties(value = VkProperties.class)
public class VkUser {

    private final Integer APP_ID;
    private static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";

    private final String APP_CODE;
    @Setter
    private String code;
    @Getter
    private String token;

    private final VkProperties vkProperties;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    KeywordsCollector keywordsCollector;

    @Autowired
    public VkUser(VkProperties vkProperties) {
        this.vkProperties = vkProperties;
        this.APP_CODE = this.vkProperties.getCode();
        this.APP_ID = this.vkProperties.getId();
    }

    public Map<Integer, String> createAndSendTokenAndVkId(String code) throws ClientException, ApiException {
        this.code = code;
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserAuthResponse authResponse = vk.oAuth()
                .userAuthorizationCodeFlow(APP_ID, APP_CODE, REDIRECT_URI, code)
                .execute();
        var actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
        token = actor.getAccessToken();
        Integer idVk = actor.getId();
        Map<Integer, String> userSecret = new HashMap<>();
        userSecret.put(idVk, token);
        return userSecret;
    }

    private UserActor createActorFromToken(String token, Integer vkId) {
        return new UserActor(vkId, token);
    }

    public List<String> checkNewsVk(String code, String token, UserRepository userRepository,
                                    Integer vkId, Long userId) throws ClientException, ApiException {
        var answerList = new ArrayList<String>();
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = createActorFromToken(token, vkId);

        StringBuilder sb = new StringBuilder();
        var userWordsList = keywordsCollector.usersWord(userId);
        if (userWordsList.size() == 0) {
            return answerList;
        }

        var filterList = new ArrayList<Filters>();
        filterList.add(Filters.POST);
        //filterList.add(Filters.NOTE);
        try {
            var testResponse = vk.newsfeed()
                    .get(actor)
                    .returnBanned(false)
                    .count(1)
                    .execute();
        } catch (ApiAuthException e) {
            var userSecret = createAndSendTokenAndVkId(code);
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                user.get().setToken(userSecret.get(vkId));
                userRepository.save(user.get());
            }
        }

        var getResponse = vk.newsfeed()
                .get(actor)
                .filters(filterList)
                .returnBanned(false)
                .count(100)
                .startTime((int) LocalDateTime.now().with(LocalTime.MIN).toEpochSecond(ZoneOffset.ofHours(3)))
                .execute();

        var listNews = getResponse.getItems();

        for (var item : listNews) {
            String dateString = "";
            StringBuilder source = new StringBuilder();
            source.append(item.getRaw().get("source_id")).append("_").append(item.getRaw().get("post_id"));
            dateString = LocalDateTime.ofEpochSecond(Long.parseLong(item.getRaw().get("date").toString()),
                    0, ZoneOffset.ofHours(3)).format(formatter);
            for (var es : item.getRaw().entrySet()) {
                if (es.getKey().equals("text")) {
                    String news = es.getValue().toString().toLowerCase();
                    if (news.isBlank()) {
                        dateString = "";
                        source.setLength(0);
                        continue;
                    }
                    String resultNews = news.toLowerCase().replaceAll("[^A-Za-zА-Яа-я0-9 ]", " ");
                    var contain = userWordsList.stream().anyMatch(x ->
                            resultNews.contains(" " + x.toLowerCase() + " "));
                    if (contain) {
                        answerList.add(sb.append(dateString)
                                .append("\n\n").append("https://vk.com/feed?w=wall").append(source).append("\n")
                                .append(es.getValue().toString().replace("\\n", "\n"))
                                .append("\n\n").toString());
                        sb.setLength(0);
                        source.setLength(0);
                    }
                    dateString = "";
                    source.setLength(0);
                }

            }
        }
        return answerList;
    }
}
