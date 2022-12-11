package com.bot.VkParsingBot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
// TODO: 11.12.2022 стоит вынести в отдельный пакет.
//  И назвать бы его покрасивее. CheckNewsJob, например
class SheduleChecks {
    private final TelegramBot bot;
    private final UserService userService;

    @Scheduled(fixedDelay = 360000)
    public void checkNews() {
        userService.findAll()
                .forEach(x -> bot.checkUserNews(x.getId()));
    }
}
