package com.planifikausersapi.usersapi.model;

import com.planifikausersapi.usersapi.enums.UserStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserPlanifika {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;

    @Column(name = "name")
    private String name;
    
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;
}
