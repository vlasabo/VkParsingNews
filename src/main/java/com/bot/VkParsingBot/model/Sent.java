package com.bot.VkParsingBot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sent")
@AllArgsConstructor
@NoArgsConstructor
public class Sent {
    @Id
    @Getter
    @Setter
    @Column(name = "sent_news_data")
    private String sentNewsData;

    @Getter
    @Setter
    @Column(name = "user_id")
    private Long userId;
}
