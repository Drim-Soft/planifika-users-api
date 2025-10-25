package com.planifikausersapi.usersapi.service;

import com.planifikausersapi.usersapi.dto.CreateTicketRequest;
import com.planifikausersapi.usersapi.dto.TicketResponse;
import com.planifikausersapi.usersapi.dto.UpdateTicketRequest;
import com.planifikausersapi.usersapi.model.TicketStatus;
import com.planifikausersapi.usersapi.model.TicketSupport;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketStatusRepository;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketSupportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketSupportRepository ticketSupportRepository;
    private final TicketStatusRepository ticketStatusRepository;

    public TicketService(TicketSupportRepository ticketSupportRepository, 
                        TicketStatusRepository ticketStatusRepository) {
        this.ticketSupportRepository = ticketSupportRepository;
        this.ticketStatusRepository = ticketStatusRepository;
    }

    @Transactional("drimsoftTransactionManager")
    public TicketResponse createTicket(CreateTicketRequest request) {
        TicketStatus defaultStatus = ticketStatusRepository.findByName("PENDING")
            .orElseGet(() -> {
                TicketStatus newStatus = new TicketStatus();
                newStatus.setName("PENDING");
                return ticketStatusRepository.save(newStatus);
            });

        TicketSupport ticket = new TicketSupport();
        ticket.setIdPlanifikaUser(request.getIdPlanifikaUser());
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setIdTicketStatus(defaultStatus.getIdTicketStatus());
        ticket.setAnswer(null);
        ticket.setIdDrimsoftUser(null);

        TicketSupport savedTicket = ticketSupportRepository.save(ticket);
        return mapToResponse(savedTicket, defaultStatus);
    }

    public List<TicketResponse> getAllTickets() {
        List<TicketSupport> tickets = ticketSupportRepository.findAll();
        return tickets.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Integer id) {
        TicketSupport ticket = ticketSupportRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id));
        return mapToResponse(ticket);
    }

    public List<TicketResponse> getTicketsByPlanifikaUser(Integer userId) {
        List<TicketSupport> tickets = ticketSupportRepository.findByIdPlanifikaUser(userId);
        return tickets.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketsByStatus(Integer statusId) {
        List<TicketSupport> tickets = ticketSupportRepository.findByIdTicketStatus(statusId);
        return tickets.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional("drimsoftTransactionManager")
    public TicketResponse updateTicket(Integer id, UpdateTicketRequest request) {
        TicketSupport ticket = ticketSupportRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id));

        if (request.getIdTicketStatus() != null) {
            ticket.setIdTicketStatus(request.getIdTicketStatus());
        }
        if (request.getAnswer() != null) {
            ticket.setAnswer(request.getAnswer());
        }
        if (request.getIdDrimsoftUser() != null) {
            ticket.setIdDrimsoftUser(request.getIdDrimsoftUser());
        }

        TicketSupport updatedTicket = ticketSupportRepository.save(ticket);
        return mapToResponse(updatedTicket);
    }

    @Transactional("drimsoftTransactionManager")
    public void deleteTicket(Integer id) {
        if (!ticketSupportRepository.existsById(id)) {
            throw new EntityNotFoundException("Ticket not found with id: " + id);
        }
        ticketSupportRepository.deleteById(id);
    }

    private TicketResponse mapToResponse(TicketSupport ticket) {
        TicketStatus status = ticketStatusRepository.findById(ticket.getIdTicketStatus())
            .orElse(null);
        return mapToResponse(ticket, status);
    }

    private TicketResponse mapToResponse(TicketSupport ticket, TicketStatus status) {
        TicketResponse response = new TicketResponse();
        response.setIdTickets(ticket.getIdTickets());
        response.setIdPlanifikaUser(ticket.getIdPlanifikaUser());
        response.setIdTicketStatus(ticket.getIdTicketStatus());
        response.setTicketStatusName(status != null ? status.getName() : null);
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setAnswer(ticket.getAnswer());
        response.setIdDrimsoftUser(ticket.getIdDrimsoftUser());
        return response;
    }
}
