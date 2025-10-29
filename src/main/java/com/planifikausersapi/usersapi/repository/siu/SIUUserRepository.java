package com.planifikausersapi.usersapi.repository.siu;

import com.planifikausersapi.usersapi.model.UserSIU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SIUUserRepository extends JpaRepository<UserSIU, Integer> {
    Optional<UserSIU> findBySupabaseUserId(UUID supabaseUserId);
}
