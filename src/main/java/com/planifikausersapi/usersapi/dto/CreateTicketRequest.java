package com.planifikausersapi.usersapi.dto;

import lombok.Data;

@Data
public class CreateTicketRequest {
    private Integer idPlanifikaUser;
    private String title;
    private String description;
}
