package com.bot.VkParsingBot.repository;

import com.bot.VkParsingBot.model.Sent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentRepository extends JpaRepository<Sent, Long> {
}