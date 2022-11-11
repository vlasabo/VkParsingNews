package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.StreamSupport;


@Service
class SheduleChecks {
    @Autowired
    TelegramBot bot;

    @Autowired
    UserRepository userRepository;

    @Scheduled(fixedDelay = 360000)
    public void checkNews() {
        StreamSupport.stream(userRepository.findAll().spliterator(), false).forEach(x -> bot.checkUserNews(x.getId()));
        System.gc();
    }
}
