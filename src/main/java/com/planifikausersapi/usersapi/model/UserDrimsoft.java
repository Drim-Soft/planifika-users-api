package com.planifikausersapi.usersapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "userdrimsoft")
public class UserDrimsoft {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;
    
    @Column(name = "supabaseuserid")
    private UUID supabaseUserId;
    
    @Column(name = "iduserstatus")
    private Integer idUserStatus;
    
    @Column(name = "idrole")
    private Integer idRole;
    
    @Column(name = "name")
    private String name;
}
