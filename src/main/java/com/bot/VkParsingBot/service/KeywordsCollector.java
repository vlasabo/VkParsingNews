package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.Keywords;
import com.bot.VkParsingBot.repository.KeywordsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class KeywordsCollector {


    private final KeywordsRepository keywordsRepository;


    public List<String> addUsersWord(Long userId, CopyOnWriteArraySet<String> words) {
        var resultList = new ArrayList<Keywords>();
        for (String word : words) {
            Keywords keyword = new Keywords();
            keyword.setWord(word);
            keyword.setUserId(userId);
            resultList.add(keyword);
        }
        keywordsRepository.saveAll(resultList);

        return usersWord(userId);
    }

    public List<String> usersWord(Long userId) {
        return keywordsRepository.findAll()
                .stream()
                .filter(kw -> Objects.equals(kw.getUserId(), userId))
                .map(Keywords::getWord)
                .collect(Collectors.toList());
    }

    public void clearWords(Long userId) {
        keywordsRepository.deleteAll(keywordsRepository.findAll()
                .stream()
                .filter(kw -> Objects.equals(kw.getUserId(), userId))
                .collect(Collectors.toList()));
    }
}
