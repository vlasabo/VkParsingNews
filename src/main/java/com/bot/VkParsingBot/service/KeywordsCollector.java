package com.bot.VkParsingBot.service;

import org.springframework.beans.factory.annotation.Autowired;
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
        //TODO: подумать над перезаписью не всей мапы, а конкретного пользователя
        var wordsRow = this.jdbcTemplate.queryForRowSet("SELECT * FROM keywords");

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

    public List<String> usersWord(Long userId) {
        for (var es : allWordsFromDb.entrySet()) {
            if (Objects.equals(es.getKey(), userId)) {
                return Arrays.stream(es.getValue().split("\n")).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
