package com.bot.VkParsingBot.repository;

import com.bot.VkParsingBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
