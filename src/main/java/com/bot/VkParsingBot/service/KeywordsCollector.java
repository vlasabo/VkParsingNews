package com.bot.VkParsingBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class KeywordsCollector {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> usersWord(Long userId) {
        var wordsRow = jdbcTemplate.queryForRowSet("SELECT word FROM keywords WHERE user_id = ?", userId);
        List<String> allWords = new ArrayList<>();
        while (wordsRow.next()) {
            allWords.add(wordsRow.getString("word"));
        }
        return allWords;
    }
}
