package com.planifikausersapi.usersapi.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "usersiu")
public class UserSIU {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusersiu")
    private Integer idUserSIU;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "photourl")
    private String photoUrl;
    
    @Column(name = "supabaseuserid")
    private UUID supabaseUserId;
}
