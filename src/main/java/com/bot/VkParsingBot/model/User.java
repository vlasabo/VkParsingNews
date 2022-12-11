package com.bot.VkParsingBot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
// TODO: 11.12.2022 В чем логика с наличие/отсутствием геттеров? И советую всегда их выносить над
//  классом, над полями - только в исключительных случаях, либо для доп. настройки
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @Getter
    @Setter
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(name = "first_name")
    private String firstName;

    @Setter
    @Column(name = "last_name")
    private String lastName;

    @Setter
    @Column(name = "user_name")
    private String userName;

    @Setter
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Getter
    @Setter
    @Column(name = "code")
    private String code;

    @Setter
    @Getter
    @Column(name = "token")
    private String token;

    @Getter
    @Setter
    @Column(name = "vk_id")
    private Integer vkId;

    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sent", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "sent_news_data")
    // TODO: 11.12.2022 Почему связи не описаны в Entity-классах? Пусть там даже будут классы на пару полей
    private Set<String> sentNews;

    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "keywords", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "word")
//    TODO: 11.12.2022 Всегда делай базовую инициализацию для полей-коллекций в сущностях.
//     Иначе рано или поздно поймаешь NullPointerException
    private Set<String> userWordsList;
}
