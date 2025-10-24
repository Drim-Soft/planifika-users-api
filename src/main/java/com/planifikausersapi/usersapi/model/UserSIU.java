package com.planifikausersapi.usersapi.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UserSIU {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUserSIU;
    private String name;
    private String photoUrl;
    private UUID supabaseUserId;
}
