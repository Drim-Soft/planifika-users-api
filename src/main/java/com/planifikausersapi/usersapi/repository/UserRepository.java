package com.planifikausersapi.usersapi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.planifikausersapi.usersapi.dto.DtoUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<DtoUser, Integer> {
    
    Optional<DtoUser> findBySupabaseUserId(UUID supabaseUserId);
}
