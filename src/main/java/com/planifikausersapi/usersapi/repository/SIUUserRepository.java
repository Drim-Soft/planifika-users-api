package com.planifikausersapi.usersapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.planifikausersapi.usersapi.model.UserSIU;

@Repository
public interface SIUUserRepository extends JpaRepository<UserSIU, Integer> {
  Optional<UserSIU> findBySupabaseUserId(UUID supabaseUserId);
}
