package com.planifikausersapi.usersapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "ticketsupport")
public class TicketSupport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtickets")
    private Integer idTickets;
    
    @Column(name = "idplanifikauser")
    private Integer idPlanifikaUser;
    
    @Column(name = "idticketstatus")
    private Integer idTicketStatus;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "answer")
    private String answer;
    
    @Column(name = "iddrimsoftuser", nullable = true)
    private Integer idDrimsoftUser;
}
