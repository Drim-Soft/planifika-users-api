package com.planifikausersapi.usersapi.dto;

import lombok.Data;

@Data
public class TicketResponse {
    private Integer idTickets;
    private Integer idPlanifikaUser;
    private Integer idTicketStatus;
    private String ticketStatusName;
    private String title;
    private String description;
    private String answer;
    private Integer idDrimsoftUser;
}
