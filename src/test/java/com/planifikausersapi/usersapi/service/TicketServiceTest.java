package com.planifikausersapi.usersapi.service;

import com.planifikausersapi.usersapi.dto.CreateTicketRequest;
import com.planifikausersapi.usersapi.dto.TicketResponse;
import com.planifikausersapi.usersapi.dto.UpdateTicketRequest;
import com.planifikausersapi.usersapi.model.TicketStatus;
import com.planifikausersapi.usersapi.model.TicketSupport;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketStatusRepository;
import com.planifikausersapi.usersapi.repository.drimsoft.TicketSupportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketSupportRepository ticketSupportRepository;
    
    @Mock
    private TicketStatusRepository ticketStatusRepository;

    @InjectMocks
    private TicketService ticketService;

    private TicketStatus pendingStatus;

    @BeforeEach
    void setUp() {
        pendingStatus = new TicketStatus();
        pendingStatus.setIdTicketStatus(1);
        pendingStatus.setName("PENDING");
    }

    @Test
    void createTicket_usesPendingStatus_whenExists() {
        // Arrange
        CreateTicketRequest req = new CreateTicketRequest();
        req.setIdPlanifikaUser(9);
        req.setTitle("Problema de login");
        req.setDescription("No puedo entrar");

        when(ticketStatusRepository.findByName("PENDING")).thenReturn(Optional.of(pendingStatus));

        TicketSupport saved = new TicketSupport();
        saved.setIdTickets(100);
        saved.setIdPlanifikaUser(9);
        saved.setIdTicketStatus(1);
        saved.setTitle("Problema de login");
        saved.setDescription("No puedo entrar");

        when(ticketSupportRepository.save(any(TicketSupport.class))).thenReturn(saved);

        // Act
        TicketResponse res = ticketService.createTicket(req);

        // Assert
        assertThat(res.getIdTickets()).isEqualTo(100);
        assertThat(res.getIdPlanifikaUser()).isEqualTo(9);
        assertThat(res.getIdTicketStatus()).isEqualTo(1);
        assertThat(res.getTicketStatusName()).isEqualTo("PENDING");
        verify(ticketSupportRepository, times(1)).save(any(TicketSupport.class));
    }

    @Test
    void createTicket_createsPendingStatus_whenMissing() {
        // Arrange
        CreateTicketRequest req = new CreateTicketRequest();
        req.setIdPlanifikaUser(5);
        req.setTitle("Bug");
        req.setDescription("detalle");

        when(ticketStatusRepository.findByName("PENDING")).thenReturn(Optional.empty());

        TicketStatus created = new TicketStatus();
        created.setIdTicketStatus(1);
        created.setName("PENDING");
        when(ticketStatusRepository.save(any(TicketStatus.class))).thenReturn(created);

        TicketSupport saved = new TicketSupport();
        saved.setIdTickets(77);
        saved.setIdPlanifikaUser(5);
        saved.setIdTicketStatus(1);
        saved.setTitle("Bug");
        saved.setDescription("detalle");
        when(ticketSupportRepository.save(any(TicketSupport.class))).thenReturn(saved);

        // Act
        TicketResponse res = ticketService.createTicket(req);

        // Assert
        assertThat(res.getIdTickets()).isEqualTo(77);
        assertThat(res.getTicketStatusName()).isEqualTo("PENDING");
        verify(ticketStatusRepository, times(1)).save(any(TicketStatus.class));
    }

    @Test
    void getTicketById_returnsMappedResponse() {
        TicketSupport t = new TicketSupport();
        t.setIdTickets(10);
        t.setIdPlanifikaUser(9);
        t.setIdTicketStatus(2);
        t.setTitle("Titulo");
        t.setDescription("Desc");
        when(ticketSupportRepository.findById(10)).thenReturn(Optional.of(t));

        TicketStatus st = new TicketStatus();
        st.setIdTicketStatus(2);
        st.setName("IN_PROGRESS");
        when(ticketStatusRepository.findById(2)).thenReturn(Optional.of(st));

        TicketResponse res = ticketService.getTicketById(10);

        assertThat(res.getTicketStatusName()).isEqualTo("IN_PROGRESS");
        assertThat(res.getTitle()).isEqualTo("Titulo");
    }

    @Test
    void getTicketById_throwsWhenMissing() {
        when(ticketSupportRepository.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ticketService.getTicketById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateTicket_updatesOnlyProvidedFields() {
        TicketSupport t = new TicketSupport();
        t.setIdTickets(1);
        t.setIdTicketStatus(1);
        t.setAnswer(null);
        t.setIdDrimsoftUser(null);
        when(ticketSupportRepository.findById(1)).thenReturn(Optional.of(t));

        UpdateTicketRequest req = new UpdateTicketRequest();
        req.setIdTicketStatus(3);
        req.setAnswer("Listo");
        // idDrimsoftUser ausente (null)

        TicketSupport updated = new TicketSupport();
        updated.setIdTickets(1);
        updated.setIdTicketStatus(3);
        updated.setAnswer("Listo");
        updated.setIdDrimsoftUser(null);
        when(ticketSupportRepository.save(any(TicketSupport.class))).thenReturn(updated);

        TicketResponse res = ticketService.updateTicket(1, req);

        assertThat(res.getIdTicketStatus()).isEqualTo(3);
        assertThat(res.getAnswer()).isEqualTo("Listo");
        // verify that repository.save got entity with desired fields
        verify(ticketSupportRepository).save(Mockito.argThat(arg ->
                arg.getIdTicketStatus().equals(3) &&
                "Listo".equals(arg.getAnswer()) &&
                arg.getIdDrimsoftUser() == null
        ));
    }

    @Test
    void deleteTicket_throwsWhenNotExists() {
        when(ticketSupportRepository.existsById(123)).thenReturn(false);
        assertThatThrownBy(() -> ticketService.deleteTicket(123))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("123");
        verify(ticketSupportRepository, never()).deleteById(any());
    }

    @Test
    void deleteTicket_deletesWhenExists() {
        when(ticketSupportRepository.existsById(5)).thenReturn(true);
        ticketService.deleteTicket(5);
        verify(ticketSupportRepository, times(1)).deleteById(eq(5));
    }
}
