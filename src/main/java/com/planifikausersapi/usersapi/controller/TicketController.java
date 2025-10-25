package com.planifikausersapi.usersapi.controller;

import com.planifikausersapi.usersapi.dto.CreateTicketRequest;
import com.planifikausersapi.usersapi.dto.TicketResponse;
import com.planifikausersapi.usersapi.dto.UpdateTicketRequest;
import com.planifikausersapi.usersapi.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody CreateTicketRequest request) {
        TicketResponse ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        List<TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Integer id) {
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByUser(@PathVariable Integer userId) {
        List<TicketResponse> tickets = ticketService.getTicketsByPlanifikaUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/status/{statusId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByStatus(@PathVariable Integer statusId) {
        List<TicketResponse> tickets = ticketService.getTicketsByStatus(statusId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Integer id,
            @RequestBody UpdateTicketRequest request) {
        TicketResponse ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTicket(@PathVariable Integer id) {
        ticketService.deleteTicket(id);
        Map<String, Object> response = Map.of(
            "status", 200,
            "detail", "Ticket deleted successfully"
        );
        return ResponseEntity.ok(response);
    }
}
