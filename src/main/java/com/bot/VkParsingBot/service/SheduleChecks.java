package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
class SheduleChecks {
    @Autowired
    TelegramBot bot;

    @Autowired
    UserRepository userRepository;

    @Scheduled(fixedDelay = 360000)
    public void checkNews() {
        userRepository.findAll()
                .forEach(x -> bot.checkUserNews(x.getId()));
    }
}
