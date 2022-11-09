package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.VkConfig;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.newsfeed.Filters;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VkUser {
    @Value("${app.id}")
    private final Integer APP_ID;
    private static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";

    private final String APP_CODE;
    @Setter
    private String code;
    @Getter
    private String token;

    private final VkConfig vkConfig;

    @Autowired
    KeywordsCollector keywordsCollector;

    @Autowired
    public VkUser(VkConfig vkConfig) {
        this.vkConfig = vkConfig;
        this.APP_CODE = this.vkConfig.getCode();
        this.APP_ID = this.vkConfig.getId();
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

    public List<String> checkNewsVk(String token, Integer vkId, Long userId) throws ClientException, ApiException {
        var answerList = new ArrayList<String>();
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = createActorFromToken(token, vkId);
        StringBuilder sb = new StringBuilder();
        var userWordsList = keywordsCollector.usersWord(userId);

        var filterList = new ArrayList<Filters>();
        filterList.add(Filters.POST);
        filterList.add(Filters.NOTE);
        var getResponse = vk.newsfeed()
                .get(actor)
                .filters(filterList)
                .returnBanned(true)
                .count(100)
                //.startFrom("" + LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.ofHours(3)))
                .execute();
        var listNews = getResponse.getItems();

        for (var item : listNews) {
            String dateString = "";

            for (var es : item.getRaw().entrySet()) {
                if (es.getKey().equals("date")) {
                    long ti = Long.parseLong(es.getValue().toString());
                    dateString = LocalDateTime.ofEpochSecond(ti, 0, ZoneOffset.ofHours(3)).toString()
                            .concat(": ");
                }

                if (es.getKey().equals("text")) {
                    String news = es.getValue().toString().toLowerCase();
                    if (news.isBlank()) {
                        dateString = "";
                        continue;
                    }
                    var contain = userWordsList.stream().anyMatch(x -> news.contains(" ".concat(x.toLowerCase()).concat(" ")));
                    if (contain) {
                        answerList.add(sb.append(dateString)
                                .append(es.getValue().toString().replace("\\n", "\n"))
                                .append("\n\n").toString());
                        sb.setLength(0);
                    }
                    dateString = "";
                }

            }
        }
        return answerList;
    }
}
