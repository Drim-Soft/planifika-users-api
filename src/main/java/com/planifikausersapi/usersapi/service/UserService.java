package com.planifikausersapi.usersapi.service;

import org.springframework.stereotype.Service;
import com.planifikausersapi.usersapi.enums.UserStatusEnum;
import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserPlanifika> findAll() {
        return userRepository.findAll();
    }

    public Optional<UserPlanifika> findById(Integer id) {
        return userRepository.findById(id);
    }

    public UserPlanifika save(UserPlanifika user) {
        return userRepository.save(user);
    }

    public UserPlanifika updateStatus(Integer userId, String statusName) {
        UserPlanifika user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + userId));

        try {
            UserStatusEnum status = UserStatusEnum.valueOf(statusName.toUpperCase());
            user.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + statusName);
        }

        return userRepository.save(user);
    }

    public void delete(Integer id) {
        UserPlanifika user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatusEnum.DELETED);
        userRepository.save(user);
    }


}
