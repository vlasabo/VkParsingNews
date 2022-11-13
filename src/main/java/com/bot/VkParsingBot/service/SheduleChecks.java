package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;


@Service
class SheduleChecks {
    @Autowired
    TelegramBot bot;

    @Autowired
    UserRepository userRepository;

    @Scheduled(fixedDelay = 360000)
    public void checkNews() {
//        TODO: для функционлаьного стиля: одна строчка - одна точка.
//         Конкретно здесь тебе еще и неудачный предок репозитория жизнь подпортил
        StreamSupport.stream(userRepository.findAll().spliterator(), false).forEach(x -> bot.checkUserNews(x.getId()));
//       TODO: управлять gc из клиентского кода - неблагодарное занятие. Все равно проигнорит)
        System.gc();
    }

    @Scheduled(cron = "0 0 0 * * *")
//    TODO: я так понимаю, эта чатсь еще недописана?
    public void s() {
        TelegramBot.setSentNews(new HashMap<>());
    }
}
