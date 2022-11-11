package com.bot.VkParsingBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class KeywordsCollector {

    private final JdbcTemplate jdbcTemplate;

    private final HashMap<Long, String> allWordsFromDb = new HashMap<>();

    @Autowired
    KeywordsCollector(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate; //при создании, затем при любом изменении перечитываем набор данных в джаве
        var wordsRow = jdbcTemplate.queryForRowSet("SELECT * FROM keywords");

        while (wordsRow.next()) {
            Long userId = wordsRow.getLong("user_id");
            String word = wordsRow.getString("word");
            if (word == null) {
                continue;
            }
            if (allWordsFromDb.containsKey(userId)) { //деление на отслеживаемые слова и фразы переносом строки
                allWordsFromDb.put(userId, allWordsFromDb.get(userId).concat("\n").concat(word));
            } else {
                allWordsFromDb.put(userId, word);
            }
        }
    }

    public List<String> addUsersWord(Long userId, List<String> words) {
        String resultFromMap = allWordsFromDb.getOrDefault(userId, "");
        //var listOfWords = words.stream().map(x->x.concat("\n")).collect(Collectors.toList());
        String resultFromList = "\n" + String.join("\n", words);
        String resultString = resultFromMap.concat(resultFromList);
        allWordsFromDb.put(userId, resultString);
        try {
            for (String word : words) {
                if (!word.isBlank()) {
                    jdbcTemplate.update("INSERT  INTO keywords (user_id,word) VALUES(?1,?2)", userId, word);
                }
            }
            return Arrays.stream(resultString.split("\n")).filter(x -> !x.isBlank()).collect(Collectors.toList());
        } catch (DataAccessException e) {
            allWordsFromDb.put(userId, resultFromMap);
            return usersWord(userId);
        }


    }

    public List<String> usersWord(Long userId) {
        for (var es : allWordsFromDb.entrySet()) {
            if (Objects.equals(es.getKey(), userId)) {
                return Arrays.stream(es.getValue().split("\n")).filter(x -> !x.isBlank()).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    public void clearWords(Long userId) {
        jdbcTemplate.update("DELETE FROM keywords WHERE user_id = ?", userId);
        allWordsFromDb.remove(userId);
    }
}
