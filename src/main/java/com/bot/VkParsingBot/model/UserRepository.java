package com.bot.VkParsingBot.model;

import org.springframework.data.repository.CrudRepository;

//TODO: стоит вынести в отдельный пакет
//TODO: в чем мотивация наследоваться от CrudRepository, а не JpaRepository?
public interface UserRepository extends CrudRepository<User, Long> {
}
