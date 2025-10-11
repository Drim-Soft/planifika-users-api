package com.planifikausersapi.usersapi.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Integer userId;
    private String name;
    private String role;
}
