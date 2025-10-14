package com.planifikausersapi.usersapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.planifikausersapi.usersapi.dto.DtoUser;
import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {   

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    

    @GetMapping
    public ResponseEntity<List<UserPlanifika>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserPlanifika> getUserById(@PathVariable Integer id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DtoUser> createUser(@RequestBody DtoUser user) {
        return ResponseEntity.ok(userService.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserPlanifika> updateUser(@PathVariable Integer id, @RequestBody UserPlanifika user) {
        user.setIdUser(id);
        return ResponseEntity.ok(userService.save(user));
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<UserPlanifika> updateUserStatus(@PathVariable Integer id, @PathVariable String status) {
        return ResponseEntity.ok(userService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer id) {
        userService.delete(id);
        Map<String, Object> response = Map.of(
            "status", 200,
            "detail", "Usuario eliminado correctamente"
        );
        return ResponseEntity.ok(response);
    }

}
