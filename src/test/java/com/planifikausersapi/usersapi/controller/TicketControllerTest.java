package com.planifikausersapi.usersapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planifikausersapi.usersapi.dto.CreateTicketRequest;
import com.planifikausersapi.usersapi.dto.TicketResponse;
import com.planifikausersapi.usersapi.dto.UpdateTicketRequest;
import com.planifikausersapi.usersapi.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketService ticketService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        TicketService ticketService() {
            return org.mockito.Mockito.mock(TicketService.class);
        }
    }

    private TicketResponse sampleResponse(int id, int planifikaUser, int status, String title) {
        TicketResponse r = new TicketResponse();
        r.setIdTickets(id);
        r.setIdPlanifikaUser(planifikaUser);
        r.setIdTicketStatus(status);
        r.setTicketStatusName("PENDING");
        r.setTitle(title);
        r.setDescription("desc");
        return r;
    }

    @Test
    @DisplayName("POST /tickets crea ticket y retorna 201")
    void createTicket_returns201() throws Exception {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setIdPlanifikaUser(9);
        req.setTitle("Titulo");
        req.setDescription("Desc");

        TicketResponse res = sampleResponse(1, 9, 1, "Titulo");
        given(ticketService.createTicket(any(CreateTicketRequest.class))).willReturn(res);

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTickets", is(1)))
                .andExpect(jsonPath("$.title", is("Titulo")))
                .andExpect(jsonPath("$.ticketStatusName", is("PENDING")));
    }

    @Test
    @DisplayName("GET /tickets/user/{id} devuelve lista")
    void getByUser_returnsList() throws Exception {
        List<TicketResponse> list = List.of(
                sampleResponse(1, 9, 1, "A"),
                sampleResponse(2, 9, 2, "B")
        );
        given(ticketService.getTicketsByPlanifikaUser(9)).willReturn(list);

        mockMvc.perform(get("/tickets/user/{userId}", 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idTickets", is(1)))
                .andExpect(jsonPath("$[1].idTickets", is(2)));
    }

    @Test
    @DisplayName("PUT /tickets/{id} actualiza y retorna 200")
    void update_returns200() throws Exception {
        UpdateTicketRequest req = new UpdateTicketRequest();
        req.setIdTicketStatus(3);
        req.setAnswer("Listo");

        TicketResponse res = sampleResponse(5, 9, 3, "Titulo");
        res.setAnswer("Listo");
        given(ticketService.updateTicket(eq(5), any(UpdateTicketRequest.class))).willReturn(res);

        mockMvc.perform(put("/tickets/{id}", 5)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTicketStatus", is(3)))
                .andExpect(jsonPath("$.answer", is("Listo")));
    }

    @Test
    @DisplayName("DELETE /tickets/{id} retorna 200 y mensaje")
    void delete_returns200() throws Exception {
        mockMvc.perform(delete("/tickets/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.detail", containsString("deleted")));
    }
}
