package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.Sent;
import com.bot.VkParsingBot.repository.SentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SentService {
    private final SentRepository sentRepository;

    public boolean checkSentNewsForUser(Long userId, String news) {
        return sentRepository.findBySentNewsDataAndUserId(news, userId).isEmpty();
    }

    public void save(Sent sent) {
        sentRepository.save(sent);
    }
}
