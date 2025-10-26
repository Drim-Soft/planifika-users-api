package com.planifikausersapi.usersapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planifikausersapi.usersapi.enums.UserStatusEnum;
import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public List<UserPlanifika> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public UserPlanifika findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Transactional
    public UserPlanifika findBySupabaseId(UUID supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con supabaseUserId: " + supabaseUserId));
    }

    @Transactional
    public UserPlanifika save(UserPlanifika user) {
        return userRepository.save(user);
    }

    @Transactional
    public UserPlanifika update(UserPlanifika user) {
        findById(user.getIdUser());
        return userRepository.save(user);
    }

    @Transactional
    public UserPlanifika patchUpdate(UserPlanifika updatedUser) {
        UserPlanifika existingUser = findById(updatedUser.getIdUser());

        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getPhotoUrl() != null) {
            existingUser.setPhotoUrl(updatedUser.getPhotoUrl());
        }
        if (updatedUser.getIdUserStatus() != null) {
            existingUser.setIdUserStatus(updatedUser.getIdUserStatus());
        }
        if (updatedUser.getIdUserType() != null) {
            existingUser.setIdUserType(updatedUser.getIdUserType());
        }
        if (updatedUser.getIdOrganization() != null) {
            existingUser.setIdOrganization(updatedUser.getIdOrganization());
        }

        return userRepository.save(existingUser); // Guardar los cambios
    }

    // TODO : Este método se podría eliminar próximamente, ya que el estado se puede
    // actualizar con patchUpdate()
    @Transactional
    public UserPlanifika updateStatus(Integer userId, String statusName) {
        UserPlanifika user = findById(userId);
        try {
            UserStatusEnum status = UserStatusEnum.valueOf(statusName.toUpperCase());
            user.setIdUserStatus(status.getId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + statusName);
        }

        return userRepository.save(user);
    }

    @Transactional
    public UserPlanifika delete(Integer id) {
        UserPlanifika user = findById(id);
        user.setIdUserStatus(UserStatusEnum.DELETED.getId());
        return userRepository.save(user);
    }
}
