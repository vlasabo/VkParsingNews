package com.bot.VkParsingBot.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

//TODO: будь осторожнее с аннотацией @Data, в энтити ее вообще совать не стоит:
// - она включает в себя @EqualsAndHashCode, что не рекомендуется для энтити-классов
// - она включает в себя только @RequiredArgsConstructor. Энтити требует конструктор без параметров.
// Т.е. если в этом классе появится какое-нибудь @NonNull поле - все сломается.
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
//    TODO: LocalDateTime. ORM обработает, а работать будет удобнее
    private Timestamp registrationDate;

    @Column(name = "code")
//    TODO: не знаю назначение поля, возможно, стоит подумтаь о том, чтобы заменить на Enum
    private String code;

    @Column(name = "token")
    private String token;

    @Column(name = "vk_id")
    private Integer vkId;

    @Transient
//    TODO: не знаю назначение поля, не уверен, что нужен @Transient.
//     Мб подошла бы отдельная сущность с M2M связью или просто колонка типа текстовый массив
    private List<String> trackedWords;
}
