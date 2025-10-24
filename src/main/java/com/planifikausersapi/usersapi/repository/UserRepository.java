package com.planifikausersapi.usersapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.planifikausersapi.usersapi.model.UserPlanifika;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserPlanifika, Integer> {

    Optional<UserPlanifika> findBySupabaseUserId(UUID supabaseUserId);
}
