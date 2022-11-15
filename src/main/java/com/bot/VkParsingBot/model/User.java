package com.bot.VkParsingBot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
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

}
