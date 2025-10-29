package com.planifikausersapi.usersapi.repository.drimsoft;

import com.planifikausersapi.usersapi.model.UserDrimsoft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDrimsoftRepository extends JpaRepository<UserDrimsoft, Integer> {
    Optional<UserDrimsoft> findBySupabaseUserId(UUID supabaseUserId);
}
