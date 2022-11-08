package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.VkConfig;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
}
