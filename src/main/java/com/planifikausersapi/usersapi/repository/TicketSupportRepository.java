package com.planifikausersapi.usersapi.repository;

import com.planifikausersapi.usersapi.model.TicketSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketSupportRepository extends JpaRepository<TicketSupport, Integer> {
    List<TicketSupport> findByIdPlanifikaUser(Integer idPlanifikaUser);
    List<TicketSupport> findByIdTicketStatus(Integer idTicketStatus);
}
