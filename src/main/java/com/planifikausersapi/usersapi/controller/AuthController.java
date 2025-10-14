package com.planifikausersapi.usersapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.planifikausersapi.usersapi.service.AuthService;
import com.planifikausersapi.usersapi.service.SIUAuthService;

import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SIUAuthService siuAuthService;

    public AuthController(AuthService authService, SIUAuthService siuAuthService) {
        this.authService = authService;
        this.siuAuthService = siuAuthService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<Map>> signup(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        return authService.signUp(email, password)
                .map(resp -> ResponseEntity.ok(resp))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))));
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

    @PostMapping("/external-login")
    public Mono<ResponseEntity<Map>> externalLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        return siuAuthService.signIn(email, password)
                .map(resp -> ResponseEntity.ok(resp))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).body(Map.of("error", e.getMessage()))));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<Map>> me(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replaceFirst("Bearer ", "");
        return authService.getUser(token).map(ResponseEntity::ok);
    }
}
