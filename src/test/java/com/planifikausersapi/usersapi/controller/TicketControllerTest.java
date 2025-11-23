package com.planifikausersapi.usersapi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.planifikausersapi.usersapi.service.TicketService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para TicketController")
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private Map<String, Object> testTicket;

    @BeforeEach
    void setUp() {
        testTicket = new HashMap<>();
        testTicket.put("idTickets", 1);
        testTicket.put("idPlanifikaUser", 100);
        testTicket.put("title", "Test Ticket");
        testTicket.put("description", "Test Description");
        testTicket.put("idTicketStatus", 1);
        testTicket.put("ticketStatusName", "PENDING");
    }

    @Test
    @DisplayName("Debería crear ticket exitosamente")
    void testCreateTicket_Success() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("idPlanifikaUser", 100);
        request.put("title", "Test Ticket");
        request.put("description", "Test Description");
        
        when(ticketService.createTicket(100, "Test Ticket", "Test Description"))
            .thenReturn(testTicket);

        // When
        ResponseEntity<Map<String, Object>> response = ticketController.createTicket(request);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().get("idTickets"));
        verify(ticketService, times(1)).createTicket(100, "Test Ticket", "Test Description");
    }

    @Test
    @DisplayName("Debería retornar tickets paginados exitosamente")
    void testGetTicketsPaged_Success() {
        // Given
        Map<String, Object> pagedResponse = new HashMap<>();
        pagedResponse.put("items", Arrays.asList(testTicket));
        pagedResponse.put("page", 0);
        pagedResponse.put("size", 10);
        pagedResponse.put("totalElements", 1L);
        pagedResponse.put("totalPages", 1);
        
        when(ticketService.getTicketsPaged(0, 10)).thenReturn(pagedResponse);

        // When
        ResponseEntity<Map<String, Object>> response = ticketController.getTicketsPaged(0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(ticketService, times(1)).getTicketsPaged(0, 10);
    }

    @Test
    @DisplayName("Debería retornar todos los tickets exitosamente")
    void testGetAllTickets_Success() {
        // Given
        List<Map<String, Object>> tickets = Arrays.asList(testTicket);
        when(ticketService.getAllTickets()).thenReturn(tickets);

        // When
        ResponseEntity<List<Map<String, Object>>> response = ticketController.getAllTickets();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(ticketService, times(1)).getAllTickets();
    }

    @Test
    @DisplayName("Debería retornar ticket por ID exitosamente")
    void testGetTicketById_Success() {
        // Given
        when(ticketService.getTicketById(1)).thenReturn(testTicket);

        // When
        ResponseEntity<Map<String, Object>> response = ticketController.getTicketById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().get("idTickets"));
        verify(ticketService, times(1)).getTicketById(1);
    }

    @Test
    @DisplayName("Debería retornar tickets por usuario exitosamente")
    void testGetTicketsByUser_Success() {
        // Given
        List<Map<String, Object>> tickets = Arrays.asList(testTicket);
        when(ticketService.getTicketsByPlanifikaUser(100)).thenReturn(tickets);

        // When
        ResponseEntity<List<Map<String, Object>>> response = ticketController.getTicketsByUser(100);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(ticketService, times(1)).getTicketsByPlanifikaUser(100);
    }

    @Test
    @DisplayName("Debería retornar tickets por estado exitosamente")
    void testGetTicketsByStatus_Success() {
        // Given
        List<Map<String, Object>> tickets = Arrays.asList(testTicket);
        when(ticketService.getTicketsByStatus(1)).thenReturn(tickets);

        // When
        ResponseEntity<List<Map<String, Object>>> response = ticketController.getTicketsByStatus(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(ticketService, times(1)).getTicketsByStatus(1);
    }

    @Test
    @DisplayName("Debería actualizar ticket exitosamente")
    void testUpdateTicket_Success() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("idTicketStatus", 2);
        request.put("answer", "Updated Answer");
        request.put("idDrimsoftUser", 50);
        
        Map<String, Object> updatedTicket = new HashMap<>(testTicket);
        updatedTicket.put("idTicketStatus", 2);
        updatedTicket.put("answer", "Updated Answer");
        
        when(ticketService.updateTicket(1, 2, "Updated Answer", 50))
            .thenReturn(updatedTicket);

        // When
        ResponseEntity<Map<String, Object>> response = ticketController.updateTicket(1, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(ticketService, times(1)).updateTicket(1, 2, "Updated Answer", 50);
    }

    @Test
    @DisplayName("Debería actualizar ticket con valores null")
    void testUpdateTicket_WithNulls() {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("idTicketStatus", null);
        request.put("answer", null);
        request.put("idDrimsoftUser", null);
        
        when(ticketService.updateTicket(1, null, null, null))
            .thenReturn(testTicket);

        // When
        ResponseEntity<Map<String, Object>> response = ticketController.updateTicket(1, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(ticketService, times(1)).updateTicket(1, null, null, null);
    }

    @Test
    @DisplayName("Debería eliminar ticket exitosamente")
    void testDeleteTicket_Success() {
        // Given
        doNothing().when(ticketService).deleteTicket(1);

        // When
        ResponseEntity<Map<String, Object>> response = ticketController.deleteTicket(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().get("status"));
        verify(ticketService, times(1)).deleteTicket(1);
    }

    @Test
    @DisplayName("Debería usar valores por defecto en getTicketsPaged")
    void testGetTicketsPaged_DefaultValues() {
        // Given
        Map<String, Object> pagedResponse = new HashMap<>();
        pagedResponse.put("items", Arrays.asList());
        pagedResponse.put("page", 0);
        pagedResponse.put("size", 10);
        
        when(ticketService.getTicketsPaged(0, 10)).thenReturn(pagedResponse);

        // When
        ResponseEntity<Map<String, Object>> response = ticketController.getTicketsPaged(0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

