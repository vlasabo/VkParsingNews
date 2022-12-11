package com.bot.VkParsingBot.service;

import com.bot.VkParsingBot.model.User;
import com.bot.VkParsingBot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // TODO: 11.12.2022 create()
    public void save(User user) {
        userRepository.save(user);
    }

    // TODO: 11.12.2022 getAll(). Поосторожнее с таким. Пользователей и миллион мб
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
