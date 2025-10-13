package com.planifikausersapi.usersapi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.planifikausersapi.usersapi.model.UserPlanifika;

@Repository
public interface UserRepository extends JpaRepository<UserPlanifika, Integer> {
    
}
