package com.bot.VkParsingBot.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "keywords")
public class UserKeywords {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "word")
    private String word;
}
