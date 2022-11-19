package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.config.VkProperties;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@EnableConfigurationProperties(value = VkProperties.class)
public class VkUser {

    private final Integer APP_ID;
    private static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    private final String APP_CODE;

    @Autowired
    public VkUser(VkProperties vkProperties) {
        this.APP_CODE = vkProperties.getCode();
        this.APP_ID = vkProperties.getId();
    }

    public Map<Integer, String> createAndSendTokenAndVkId(String code) throws ClientException, ApiException {
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(transportClient);
        UserAuthResponse authResponse = vk.oAuth()
                .userAuthorizationCodeFlow(APP_ID, APP_CODE, REDIRECT_URI, code)
                .execute();
        var actor = createActorFromToken(authResponse.getAccessToken(), authResponse.getUserId());
        String token = actor.getAccessToken();
        Integer idVk = actor.getId();
        Map<Integer, String> userSecret = new HashMap<>();
        userSecret.put(idVk, token);
        return userSecret;
    }

    public UserActor createActorFromToken(String token, Integer vkId) {
        return new UserActor(vkId, token);
    }


}
