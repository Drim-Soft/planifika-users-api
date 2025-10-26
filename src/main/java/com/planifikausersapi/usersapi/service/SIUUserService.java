package com.planifikausersapi.usersapi.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planifikausersapi.usersapi.model.UserSIU;

import com.planifikausersapi.usersapi.repository.SIUUserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SIUUserService {

  private final SIUUserRepository siuUserRepository;

  public SIUUserService(SIUUserRepository siuUserRepository) {
    this.siuUserRepository = siuUserRepository;
  }

  @Transactional
  public UserSIU findBySupabaseId(UUID supabaseUserId) {
    return siuUserRepository.findBySupabaseUserId(supabaseUserId)
        .orElseThrow(() -> new EntityNotFoundException(
            "Usuario no encontrado con supabaseUserId: " + supabaseUserId));
  }
}
