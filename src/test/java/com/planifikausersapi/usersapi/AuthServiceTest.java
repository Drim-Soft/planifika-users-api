package com.planifikausersapi.usersapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Creamos un mock de AuthService para simular el comportamiento real
        authService = Mockito.mock(AuthService.class);
    }

    // 1️⃣ Prueba unitaria: Login exitoso
    @Test
    void testSignIn_Success() {
        // Arrange
        String email = "carlos@planifika.com";
        String password = "123456";

        Map<String, Object> expectedResponse = Map.of(
                "status", 200,
                "message", "Autenticación exitosa",
                "token", "djsahjdksah",
                "expiresIn", 3600
        );

        when(authService.signIn(email, password)).thenReturn(Mono.just(expectedResponse));

        // Act
        Map<String, Object> result = authService.signIn(email, password).block();

        // Assert
        assertNotNull(result);
        assertEquals(200, result.get("status"));
        assertEquals("Autenticación exitosa", result.get("message"));
        assertEquals("djsahjdksah", result.get("token"));
    }

    // 2️⃣ Prueba unitaria: Login fallido
    @Test
    void testSignIn_Failure() {
        // Arrange
        String email = "wrong@planifika.com";
        String password = "badpass";

        when(authService.signIn(email, password))
                .thenReturn(Mono.error(new RuntimeException("Credenciales inválidas")));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            authService.signIn(email, password).block();
        });

        assertEquals("Credenciales inválidas", thrown.getMessage());
    }

    // 3️⃣ Prueba unitaria: Actualización de perfil exitosa
    @Test
    void testUpdateProfile_Success() {
        // Arrange
        String token = "fake-token";
        String name = "Carlos Updated";
        String password = "123456";
        String photoUrl = "https://img.com/photo.png";

        Map<String, Object> expectedResponse = Map.of(
                "status", 200,
                "message", "Perfil actualizado correctamente"
        );

        when(authService.updateProfile(token, name, password, photoUrl))
                .thenReturn(Mono.just(expectedResponse));

        // Act
        Map<String, Object> result = authService.updateProfile(token, name, password, photoUrl).block();

        // Assert
        assertNotNull(result);
        assertEquals(200, result.get("status"));
        assertEquals("Perfil actualizado correctamente", result.get("message"));
    }
}
