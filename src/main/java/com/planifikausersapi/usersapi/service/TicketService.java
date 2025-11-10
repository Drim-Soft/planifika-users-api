package com.planifikausersapi.usersapi.service;

import com.planifikausersapi.usersapi.model.TicketStatus;
import com.planifikausersapi.usersapi.model.TicketSupport;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketStatusRepository;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketSupportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Map<String, Object> createTicket(Integer idPlanifikaUser, String title, String description) {
        TicketStatus defaultStatus = ticketStatusRepository.findByName("PENDING")
            .orElseGet(() -> {
                TicketStatus newStatus = new TicketStatus();
                newStatus.setName("PENDING");
                return ticketStatusRepository.save(newStatus);
            });

        TicketSupport ticket = new TicketSupport();
        ticket.setIdPlanifikaUser(idPlanifikaUser);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setIdTicketStatus(defaultStatus.getIdTicketStatus());
        ticket.setAnswer(null);
        ticket.setIdDrimsoftUser(null);

        TicketSupport savedTicket = ticketSupportRepository.save(ticket);
        return mapToResponse(savedTicket, defaultStatus);
    }

    public List<Map<String, Object>> getAllTickets() {
        List<TicketSupport> tickets = ticketSupportRepository.findAll();
        return tickets.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, value = "drimsoftTransactionManager")
    public Map<String, Object> getTicketsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "idTickets"));
        Page<TicketSupport> pageResult = ticketSupportRepository.findAll(pageable);

        List<Map<String, Object>> items = pageResult.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("page", pageResult.getNumber());
        response.put("size", pageResult.getSize());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("hasNext", pageResult.hasNext());
        response.put("hasPrevious", pageResult.hasPrevious());
        return response;
    }

    public Map<String, Object> getTicketById(Integer id) {
        TicketSupport ticket = ticketSupportRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id));
        return mapToResponse(ticket);
    }

    public List<Map<String, Object>> getTicketsByPlanifikaUser(Integer userId) {
        List<TicketSupport> tickets = ticketSupportRepository.findByIdPlanifikaUser(userId);
        return tickets.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTicketsByStatus(Integer statusId) {
        List<TicketSupport> tickets = ticketSupportRepository.findByIdTicketStatus(statusId);
        return tickets.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional("drimsoftTransactionManager")
    public Map<String, Object> updateTicket(Integer id, Integer idTicketStatus, String answer, Integer idDrimsoftUser) {
        TicketSupport ticket = ticketSupportRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id));

        if (idTicketStatus != null) {
            ticket.setIdTicketStatus(idTicketStatus);
        }
        if (answer != null) {
            ticket.setAnswer(answer);
        }
        if (idDrimsoftUser != null) {
            ticket.setIdDrimsoftUser(idDrimsoftUser);
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

    private Map<String, Object> mapToResponse(TicketSupport ticket) {
        TicketStatus status = ticketStatusRepository.findById(ticket.getIdTicketStatus())
            .orElse(null);
        return mapToResponse(ticket, status);
    }

    private Map<String, Object> mapToResponse(TicketSupport ticket, TicketStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("idTickets", ticket.getIdTickets());
        response.put("idPlanifikaUser", ticket.getIdPlanifikaUser());
        response.put("idTicketStatus", ticket.getIdTicketStatus());
        response.put("ticketStatusName", status != null ? status.getName() : null);
        response.put("title", ticket.getTitle());
        response.put("description", ticket.getDescription());
        response.put("answer", ticket.getAnswer());
        response.put("idDrimsoftUser", ticket.getIdDrimsoftUser());
        return response;
    }
}
