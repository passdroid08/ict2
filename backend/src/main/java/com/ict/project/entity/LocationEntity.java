package com.ict.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LOCATION")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LocationEntity {

    @Id
    @Column(name = "REGION_CODE", length = 20, nullable = false)
    private String regionCode;

    @Column(name = "SIDO", length = 30)
    private String sido;

    @Column(name = "SIGUNGU", length = 30)
    private String sigungu;

    @Column(name = "DONG", length = 30)
    private String dong;

    @Column(name = "REGULATION_TYPE_LIST", length = 50)
    private String regulationTypeList;
}
