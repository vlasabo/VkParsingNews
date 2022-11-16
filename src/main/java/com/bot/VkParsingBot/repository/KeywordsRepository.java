package com.bot.VkParsingBot.repository;

import com.bot.VkParsingBot.model.Keywords;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordsRepository extends JpaRepository<Keywords, Long> {
}