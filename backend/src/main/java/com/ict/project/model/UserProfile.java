package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER_PROFILE")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @Column(name = "NAME", length = 50)
    private String name;

    @Column(name = "AGE")
    private Integer age;

    @Column(name = "GENDER", length = 10)
    private String gender;

    @Column(name = "MARITAL_STATUS", length = 20)
    private String maritalStatus;

    @Column(name = "NICKNAME", length = 50)
    private String nickname;

    @Column(name = "ADDRESS", length = 200)
    private String address;

    @Column(name = "PHONE", length = 20)
    private String phone;
}
