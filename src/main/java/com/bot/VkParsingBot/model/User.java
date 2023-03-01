package com.bot.VkParsingBot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "code")
    private String code;

    @Column(name = "token")
    private String token;

    @Column(name = "vk_id")
    private Integer vkId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sent", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "sent_news_data")
    private Set<String> sentNews = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "keywords", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "word")
    private Set<String> userWordsList = new HashSet<>();
}
