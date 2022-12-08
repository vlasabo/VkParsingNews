package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.User;
import com.bot.VkParsingBot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
public class KeywordsCollector {
    private final UserRepository userRepository;

    public String addUsersWord(User user, CopyOnWriteArraySet<String> resultWordsListForUser) {
        for (String word : resultWordsListForUser) {
            user.getUserWordsList().add(word);
        }
        userRepository.save(user);
        return user.getUserWordsList().toString();
    }
}
