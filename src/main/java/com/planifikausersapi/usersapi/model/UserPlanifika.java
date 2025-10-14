package com.planifikausersapi.usersapi.model;

import com.planifikausersapi.usersapi.dto.DtoUser;
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
    private String photoUrl;
    private Integer idUserStatus;
    private Integer idUserType;
    private Integer idOrganization;
    private String supabaseUserId;

    // Getters y Setters

    public Integer getIdUser() {
        return idUser;
    }
    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    public Integer getIdUserStatus() {
        return idUserStatus;
    }
    public void setIdUserStatus(Integer idUserStatus) {
        this.idUserStatus = idUserStatus;
    }
    public Integer getIdUserType() {
        return idUserType;
    }
    public void setIdUserType(Integer idUserType) {
        this.idUserType = idUserType;
    }
    public Integer getIdOrganization() {
        return idOrganization;
    }
    public void setIdOrganization(Integer idOrganization) {
        this.idOrganization = idOrganization;
    }
    public String getSupabaseUserId() {
        return supabaseUserId;
    }
    public void setSupabaseUserId(String supabaseUserId) {
        this.supabaseUserId = supabaseUserId;
    }

    public DtoUser toDtoUser() {
        DtoUser dtoUser = new DtoUser();
        dtoUser.setIdUser(this.idUser);
        dtoUser.setName(this.name);
        dtoUser.setPhotoUrl(this.photoUrl);
        dtoUser.setIdUserStatus(this.idUserStatus);
        dtoUser.setIdUserType(this.idUserType);
        dtoUser.setIdOrganization(this.idOrganization);
        dtoUser.setSupabaseUserId(this.supabaseUserId != null ? java.util.UUID.fromString(this.supabaseUserId) : null);
        return dtoUser;
    }
}
