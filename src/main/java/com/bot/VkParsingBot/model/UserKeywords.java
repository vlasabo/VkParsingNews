package com.bot.VkParsingBot.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
//TODO: никогда не делай название класса, отличающееся от названия табилцы. Ну, кроме замены
// кэмелСтайла на снейк_стайл
@Table(name = "keywords")
public class UserKeywords {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "user_id", nullable = false)
//    TODO: тут часом @ManyToOne не нужна?
    private Long userId;

    @Column(name = "word")
    private String word;

//    TODO: по идее, ключевые слова могут совпадать у разных юзеров. Мб подумать над М2М-связью?
//    С другой стороны, есть ли вообще смысл хранить эти слова в отдельной сущности? Вполне вероятно,
//    что массив внутри юзера будет более оптимальным решением
}
