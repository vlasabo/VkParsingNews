package com.bot.VkParsingBot.model;

import javax.persistence.*;

@Entity
@Table(name = "keywords")
public class Keywords {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "word")
    private String word;
}
