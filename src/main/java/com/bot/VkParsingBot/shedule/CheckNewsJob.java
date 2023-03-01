package com.bot.VkParsingBot.shedule;

import com.bot.VkParsingBot.service.TelegramBot;
import com.bot.VkParsingBot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
class CheckNewsJob {
    private final TelegramBot bot;
    private final UserService userService;

    @Scheduled(fixedDelay = 360000)
    public void checkNews() {
        userService.findAll()
                .forEach(x -> bot.checkUserNews(x.getId()));
    }
}
