package com.bot.VkParsingBot.repository;

import com.bot.VkParsingBot.model.Sent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentRepository extends JpaRepository<Sent, Long> {
    List<Sent> findBySentNewsDataAndUserId(String sentNewsData, Long userId);
}