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
@Table(name = "userplanifika")
public class UserPlanifika {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "photourl")
    private String photoUrl;
    
    @Column(name = "iduserstatus")
    private Integer idUserStatus;
    
    @Column(name = "idusertype")
    private Integer idUserType;
    
    @Column(name = "idorganization")
    private Integer idOrganization;
    
    @Column(name = "supabaseuserid")
    private UUID supabaseUserId;
}
