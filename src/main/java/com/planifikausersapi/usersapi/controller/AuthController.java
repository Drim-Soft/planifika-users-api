package com.planifikausersapi.usersapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import com.planifikausersapi.usersapi.service.AuthService;
import com.planifikausersapi.usersapi.service.SIUAuthService;
import com.planifikausersapi.usersapi.utils.ErrorResponse;

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
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        System.out.println("Login attempt for email: " + email);
        return authService.signIn(email, password)
                .map(resp -> ResponseEntity.ok(resp))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).body(Map.of("error", e.getMessage()))));
    }

    @PostMapping("/external-login")
    public ResponseEntity<Object> externalLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        try {
            return ResponseEntity.ok(siuAuthService.externalLogin(email, password));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            if (e instanceof Unauthorized) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> me(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replaceFirst("Bearer ", "");
        return authService.getUserWithDatabaseInfo(token)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500)
                        .body(Map.of("error", "Error al obtener información del usuario: " + e.getMessage()))));
    }

    @PatchMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> updateMe(
        @RequestHeader("Authorization") String authorization,
        @RequestBody Map<String, Object> body
    ) {
        String token = authorization.replaceFirst("Bearer ", "");
        String name = body.get("name") != null ? body.get("name").toString() : null;
        String password = body.get("password") != null ? body.get("password").toString() : null;
        String photourl = body.get("photourl") != null ? body.get("photourl").toString() : null;

        return authService.updateProfile(token, name, password, photourl)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
                ))));
    }
}
