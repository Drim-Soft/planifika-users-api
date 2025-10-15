package com.planifikausersapi.usersapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.planifikausersapi.usersapi.service.AuthService;

import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<Map<String, Object>>> signup(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String photoUrl = (String) body.get("photoUrl");
        Integer userRole = (Integer) body.get("userRole"); // Nuevo campo

        return authService.signUp(name, email, password, photoUrl, userRole)
                .map(result -> ResponseEntity.ok(result))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(Map.of("error", e.getMessage()))));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        System.out.println("Login attempt for email: " + email);
        return authService.signIn(email, password)
                .map(resp -> ResponseEntity.ok(resp))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).body(Map.of("error", e.getMessage()))));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<Map>> me(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replaceFirst("Bearer ", "");
        return authService.getUser(token).map(ResponseEntity::ok);
    }
}
