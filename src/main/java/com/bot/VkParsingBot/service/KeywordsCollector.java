package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.User;
import com.bot.VkParsingBot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
// TODO: 11.12.2022  KeywordsCollectorService
public class KeywordsCollector {
    private final UserRepository userRepository;

    // TODO: 11.12.2022 За счет того, что не объявлены отдельные энтити под ряд таблиц, получается извращение.
    //  Тебе приходится круд-операцию реализовывать через юзера. Это не очень корректно
    public String addUsersWord(User user, CopyOnWriteArraySet<String> resultWordsListForUser) {
        for (String word : resultWordsListForUser) {
            user.getUserWordsList().add(word);
        }
        userRepository.save(user);
        return user.getUserWordsList().toString();
    }
}
