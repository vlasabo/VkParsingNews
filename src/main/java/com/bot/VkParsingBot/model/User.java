package com.bot.VkParsingBot.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "users")
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
    private Timestamp registrationDate;

    @Column(name = "code")
    private String code;

    @Column(name = "token")
    private String token;

    @Column(name = "vk_id")
    private Integer vkId;

    @Transient
    private List<String> trackedWords;
}
