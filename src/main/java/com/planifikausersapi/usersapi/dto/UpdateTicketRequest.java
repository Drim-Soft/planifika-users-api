package com.planifikausersapi.usersapi.dto;

import lombok.Data;

@Data
public class UpdateTicketRequest {
    private Integer idTicketStatus;
    private String answer;
    private Integer idDrimsoftUser;
}
