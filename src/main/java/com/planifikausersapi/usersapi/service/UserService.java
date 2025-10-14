package com.planifikausersapi.usersapi.service;

import org.springframework.stereotype.Service;

import com.planifikausersapi.usersapi.dto.DtoUser;
import com.planifikausersapi.usersapi.enums.UserStatusEnum;
import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserPlanifika> findAll() {
        System.out.println("DTO Users fetched from database:");
        List<DtoUser> dtoUsers = userRepository.findAll();
        List<UserPlanifika> usersPlanifika = new ArrayList<>();
        for (DtoUser dtoUser : dtoUsers) {
            System.out.println("DTO User: " + dtoUser.getName());
            usersPlanifika.add(dtoUser.toUserPlanifika());
        }
        return usersPlanifika;
    }

    public Optional<UserPlanifika> findById(Integer id) {
        return userRepository.findById(id).isPresent() ? 
               Optional.of(userRepository.findById(id).get().toUserPlanifika()) : Optional.empty();
    }

    public UserPlanifika save(UserPlanifika user) {
        return userRepository.save(user.toDtoUser()).toUserPlanifika();
    }

    public UserPlanifika updateStatus(Integer userId, String statusName) {
        UserPlanifika user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + userId)).toUserPlanifika();

        try {
            UserStatusEnum status = UserStatusEnum.valueOf(statusName.toUpperCase());
            user.setIdUserStatus(status.hashCode());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + statusName);
        }

        return userRepository.save(user.toDtoUser()).toUserPlanifika();
    }

    public void delete(Integer id) {
        UserPlanifika user = userRepository.findById(id).isPresent() ? 
                            userRepository.findById(id).get().toUserPlanifika() : null;
        if(user != null) {
            user.setIdUserStatus(UserStatusEnum.DELETED.hashCode());
            userRepository.save(user.toDtoUser());
        }
    }


}
