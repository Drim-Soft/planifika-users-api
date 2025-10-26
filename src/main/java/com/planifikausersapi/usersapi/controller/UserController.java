package com.planifikausersapi.usersapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.service.UserService;
import com.planifikausersapi.usersapi.utils.ErrorResponse;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.findAll());
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.findById(id));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            if (e instanceof EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody UserPlanifika user) {
        try {
            return ResponseEntity.ok(userService.save(user));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Integer id, @RequestBody UserPlanifika user) {
        try {
            user.setIdUser(id);
            return ResponseEntity.ok(userService.update(user));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            if (e instanceof EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patchUpdateUser(@PathVariable Integer id, @RequestBody UserPlanifika user) {
        try {
            user.setIdUser(id);
            return ResponseEntity.ok(userService.patchUpdate(user));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            if (e instanceof EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<Object> updateUserStatus(@PathVariable Integer id, @PathVariable String status) {
        try {
            return ResponseEntity.ok(userService.updateStatus(id, status));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            if (e instanceof EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.delete(id));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            if (e instanceof EntityNotFoundException) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
