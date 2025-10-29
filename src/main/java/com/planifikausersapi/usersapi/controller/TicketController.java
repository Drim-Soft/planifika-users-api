package com.planifikausersapi.usersapi.controller;

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
    public ResponseEntity<Map<String, Object>> createTicket(@RequestBody Map<String, Object> request) {
        Integer idPlanifikaUser = (Integer) request.get("idPlanifikaUser");
        String title = (String) request.get("title");
        String description = (String) request.get("description");
        
        Map<String, Object> ticket = ticketService.createTicket(idPlanifikaUser, title, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllTickets() {
        List<Map<String, Object>> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTicketById(@PathVariable Integer id) {
        Map<String, Object> ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getTicketsByUser(@PathVariable Integer userId) {
        List<Map<String, Object>> tickets = ticketService.getTicketsByPlanifikaUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/status/{statusId}")
    public ResponseEntity<List<Map<String, Object>>> getTicketsByStatus(@PathVariable Integer statusId) {
        List<Map<String, Object>> tickets = ticketService.getTicketsByStatus(statusId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTicket(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        Integer idTicketStatus = request.get("idTicketStatus") != null ? (Integer) request.get("idTicketStatus") : null;
        String answer = request.get("answer") != null ? (String) request.get("answer") : null;
        Integer idDrimsoftUser = request.get("idDrimsoftUser") != null ? (Integer) request.get("idDrimsoftUser") : null;
        
        Map<String, Object> ticket = ticketService.updateTicket(id, idTicketStatus, answer, idDrimsoftUser);
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
