package com.planifikausersapi.usersapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.planifikausersapi.usersapi.model.TicketStatus;
import com.planifikausersapi.usersapi.model.TicketSupport;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketStatusRepository;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketSupportRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para TicketService")
class TicketServiceTest {

    @Mock
    private TicketSupportRepository ticketSupportRepository;

    @Mock
    private TicketStatusRepository ticketStatusRepository;

    @InjectMocks
    private TicketService ticketService;

    private TicketSupport testTicket;
    private TicketStatus testStatus;

    @BeforeEach
    void setUp() {
        testStatus = new TicketStatus();
        testStatus.setIdTicketStatus(1);
        testStatus.setName("PENDING");

        testTicket = new TicketSupport();
        testTicket.setIdTickets(1);
        testTicket.setIdPlanifikaUser(100);
        testTicket.setTitle("Test Ticket");
        testTicket.setDescription("Test Description");
        testTicket.setIdTicketStatus(1);
        testTicket.setAnswer(null);
        testTicket.setIdDrimsoftUser(null);
    }

    @Test
    @DisplayName("Debería crear un ticket con estado PENDING existente")
    void testCreateTicket_WithExistingStatus() {
        // Given
        when(ticketStatusRepository.findByName("PENDING")).thenReturn(Optional.of(testStatus));
        when(ticketSupportRepository.save(any(TicketSupport.class))).thenReturn(testTicket);

        // When
        Map<String, Object> result = ticketService.createTicket(100, "Test Ticket", "Test Description");

        // Then
        assertNotNull(result);
        assertEquals(1, result.get("idTickets"));
        assertEquals("Test Ticket", result.get("title"));
        assertEquals("PENDING", result.get("ticketStatusName"));
        verify(ticketStatusRepository, times(1)).findByName("PENDING");
        verify(ticketStatusRepository, never()).save(any());
        verify(ticketSupportRepository, times(1)).save(any(TicketSupport.class));
    }

    @Test
    @DisplayName("Debería crear un ticket y crear estado PENDING si no existe")
    void testCreateTicket_CreateStatusIfNotExists() {
        // Given
        TicketStatus newStatus = new TicketStatus();
        newStatus.setIdTicketStatus(1);
        newStatus.setName("PENDING");

        when(ticketStatusRepository.findByName("PENDING")).thenReturn(Optional.empty());
        when(ticketStatusRepository.save(any(TicketStatus.class))).thenReturn(newStatus);
        when(ticketSupportRepository.save(any(TicketSupport.class))).thenReturn(testTicket);

        // When
        Map<String, Object> result = ticketService.createTicket(100, "Test Ticket", "Test Description");

        // Then
        assertNotNull(result);
        verify(ticketStatusRepository, times(1)).findByName("PENDING");
        verify(ticketStatusRepository, times(1)).save(any(TicketStatus.class));
        verify(ticketSupportRepository, times(1)).save(any(TicketSupport.class));
    }

    @Test
    @DisplayName("Debería retornar todos los tickets")
    void testGetAllTickets() {
        // Given
        TicketSupport ticket2 = createTicket(2, "Ticket 2");
        List<TicketSupport> tickets = Arrays.asList(testTicket, ticket2);
        when(ticketSupportRepository.findAll()).thenReturn(tickets);
        when(ticketStatusRepository.findById(1)).thenReturn(Optional.of(testStatus));

        // When
        List<Map<String, Object>> result = ticketService.getAllTickets();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ticketSupportRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería retornar tickets paginados")
    void testGetTicketsPaged() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TicketSupport> page = new PageImpl<>(Arrays.asList(testTicket), pageable, 1);
        
        when(ticketSupportRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(ticketStatusRepository.findById(1)).thenReturn(Optional.of(testStatus));

        // When
        Map<String, Object> result = ticketService.getTicketsPaged(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.get("page"));
        assertEquals(10, result.get("size"));
        assertEquals(1L, result.get("totalElements"));
        assertEquals(1, result.get("totalPages"));
        assertNotNull(result.get("items"));
        verify(ticketSupportRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Debería retornar ticket por ID cuando existe")
    void testGetTicketById_Success() {
        // Given
        when(ticketSupportRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketStatusRepository.findById(1)).thenReturn(Optional.of(testStatus));

        // When
        Map<String, Object> result = ticketService.getTicketById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.get("idTickets"));
        assertEquals("Test Ticket", result.get("title"));
        verify(ticketSupportRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el ticket no existe")
    void testGetTicketById_NotFound() {
        // Given
        when(ticketSupportRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> ticketService.getTicketById(999));
        assertTrue(exception.getMessage().contains("Ticket not found with id: 999"));
        verify(ticketSupportRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Debería retornar tickets por usuario Planifika")
    void testGetTicketsByPlanifikaUser() {
        // Given
        TicketSupport ticket2 = createTicket(2, "Ticket 2");
        List<TicketSupport> tickets = Arrays.asList(testTicket, ticket2);
        when(ticketSupportRepository.findByIdPlanifikaUser(100)).thenReturn(tickets);
        when(ticketStatusRepository.findById(1)).thenReturn(Optional.of(testStatus));

        // When
        List<Map<String, Object>> result = ticketService.getTicketsByPlanifikaUser(100);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ticketSupportRepository, times(1)).findByIdPlanifikaUser(100);
    }

    @Test
    @DisplayName("Debería retornar tickets por estado")
    void testGetTicketsByStatus() {
        // Given
        List<TicketSupport> tickets = Arrays.asList(testTicket);
        when(ticketSupportRepository.findByIdTicketStatus(1)).thenReturn(tickets);
        when(ticketStatusRepository.findById(1)).thenReturn(Optional.of(testStatus));

        // When
        List<Map<String, Object>> result = ticketService.getTicketsByStatus(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketSupportRepository, times(1)).findByIdTicketStatus(1);
    }

    @Test
    @DisplayName("Debería actualizar ticket completamente")
    void testUpdateTicket_FullUpdate() {
        // Given
        TicketStatus newStatus = new TicketStatus();
        newStatus.setIdTicketStatus(2);
        newStatus.setName("RESOLVED");

        when(ticketSupportRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketSupportRepository.save(any(TicketSupport.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        when(ticketStatusRepository.findById(2)).thenReturn(Optional.of(newStatus));

        // When
        Map<String, Object> result = ticketService.updateTicket(1, 2, "Answer", 50);

        // Then
        assertNotNull(result);
        verify(ticketSupportRepository, times(1)).findById(1);
        verify(ticketSupportRepository, times(1)).save(any(TicketSupport.class));
    }

    @Test
    @DisplayName("Debería actualizar solo el estado del ticket")
    void testUpdateTicket_OnlyStatus() {
        // Given
        when(ticketSupportRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketSupportRepository.save(any(TicketSupport.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        when(ticketStatusRepository.findById(2)).thenReturn(Optional.of(testStatus));

        // When
        Map<String, Object> result = ticketService.updateTicket(1, 2, null, null);

        // Then
        assertNotNull(result);
        verify(ticketSupportRepository, times(1)).save(any(TicketSupport.class));
    }

    @Test
    @DisplayName("Debería actualizar solo la respuesta del ticket")
    void testUpdateTicket_OnlyAnswer() {
        // Given
        when(ticketSupportRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketSupportRepository.save(any(TicketSupport.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        when(ticketStatusRepository.findById(1)).thenReturn(Optional.of(testStatus));

        // When
        Map<String, Object> result = ticketService.updateTicket(1, null, "New Answer", null);

        // Then
        assertNotNull(result);
        assertEquals("New Answer", result.get("answer"));
        verify(ticketSupportRepository, times(1)).save(any(TicketSupport.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar ticket inexistente")
    void testUpdateTicket_NotFound() {
        // Given
        when(ticketSupportRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> ticketService.updateTicket(999, 2, "Answer", 50));
        assertTrue(exception.getMessage().contains("Ticket not found with id: 999"));
        verify(ticketSupportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería eliminar un ticket existente")
    void testDeleteTicket_Success() {
        // Given
        when(ticketSupportRepository.existsById(1)).thenReturn(true);
        doNothing().when(ticketSupportRepository).deleteById(1);

        // When
        ticketService.deleteTicket(1);

        // Then
        verify(ticketSupportRepository, times(1)).existsById(1);
        verify(ticketSupportRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar ticket inexistente")
    void testDeleteTicket_NotFound() {
        // Given
        when(ticketSupportRepository.existsById(999)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> ticketService.deleteTicket(999));
        assertTrue(exception.getMessage().contains("Ticket not found with id: 999"));
        verify(ticketSupportRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debería manejar ticket sin estado correctamente")
    void testMapToResponse_WithoutStatus() {
        // Given
        when(ticketSupportRepository.findById(1)).thenReturn(Optional.of(testTicket));
        when(ticketStatusRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = ticketService.getTicketById(1);

        // Then
        assertNotNull(result);
        assertNull(result.get("ticketStatusName"));
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay tickets")
    void testGetAllTickets_EmptyList() {
        // Given
        when(ticketSupportRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Map<String, Object>> result = ticketService.getAllTickets();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debería retornar página vacía cuando no hay tickets")
    void testGetTicketsPaged_EmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TicketSupport> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(ticketSupportRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // When
        Map<String, Object> result = ticketService.getTicketsPaged(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("totalElements"));
        assertTrue(((List<?>) result.get("items")).isEmpty());
    }

    // Helper method
    private TicketSupport createTicket(Integer id, String title) {
        TicketSupport ticket = new TicketSupport();
        ticket.setIdTickets(id);
        ticket.setIdPlanifikaUser(100);
        ticket.setTitle(title);
        ticket.setDescription("Description");
        ticket.setIdTicketStatus(1);
        return ticket;
    }
}

