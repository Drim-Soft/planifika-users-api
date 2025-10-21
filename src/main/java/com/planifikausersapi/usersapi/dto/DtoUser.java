package com.planifikausersapi.usersapi.dto;

import java.util.UUID;

import com.planifikausersapi.usersapi.model.UserPlanifika;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "userplanifika")
public class DtoUser {
    @Id
    @Column(name = "iduser", nullable = false)
    private int idUser;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "photoUrl")
    private String photoUrl;

    @Column(name = "iduserstatus")
    private Integer idUserStatus;

    @Column(name = "idusertype")
    private Integer idUserType;

    @Column(name = "idorganization")
    private Integer idOrganization;

    @Column(name = "supabaseuserid")
    private UUID supabaseUserId;

    // Getters y Setters

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int iduser) {
        this.idUser = iduser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
    public void setIdUserStatus(Integer iduserstatus) {
        this.idUserStatus = iduserstatus;
    }

    public Integer getIdUserType() {
        return idUserType;
    }

    public void setIdUserType(Integer idusertype) {
        this.idUserType = idusertype;
    }

    public Integer getIdOrganization() {
        return idOrganization;
    }

    public void setIdOrganization(Integer idorganization) {
        this.idOrganization = idorganization;
    }

    public UUID getSupabaseUserId() {
        return supabaseUserId;
    }

    public void setSupabaseUserId(UUID supabaseUserId) {
        this.supabaseUserId = supabaseUserId;
    }

    public UserPlanifika toUserPlanifika() {
        UserPlanifika user = new UserPlanifika();
        user.setIdUser(this.idUser);
        user.setName(this.name);
        user.setPhotoUrl(this.photoUrl);
        user.setIdUserStatus(this.idUserStatus);
        user.setIdUserType(this.idUserType);
        user.setIdOrganization(this.idOrganization);
        user.setSupabaseUserId(this.supabaseUserId != null ? this.supabaseUserId.toString() : null);
        return user;
    }
}
