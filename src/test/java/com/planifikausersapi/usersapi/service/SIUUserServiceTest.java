package com.planifikausersapi.usersapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planifikausersapi.usersapi.model.UserSIU;
import com.planifikausersapi.usersapi.repository.siu.SIUUserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para SIUUserService")
class SIUUserServiceTest {

    @Mock
    private SIUUserRepository siuUserRepository;

    @InjectMocks
    private SIUUserService siuUserService;

    private UserSIU testUserSIU;
    private UUID testSupabaseUserId;

    @BeforeEach
    void setUp() {
        testSupabaseUserId = UUID.randomUUID();
        testUserSIU = new UserSIU();
        testUserSIU.setIdUserSIU(1);
        testUserSIU.setName("Test Student");
        testUserSIU.setPhotoUrl("https://example.com/photo.jpg");
        testUserSIU.setSupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería retornar usuario SIU por SupabaseUserId cuando existe")
    void testFindBySupabaseId_Success() {
        // Given
        when(siuUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUserSIU));

        // When
        UserSIU result = siuUserService.findBySupabaseId(testSupabaseUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getIdUserSIU());
        assertEquals("Test Student", result.getName());
        assertEquals(testSupabaseUserId, result.getSupabaseUserId());
        verify(siuUserRepository, times(1)).findBySupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería lanzar EntityNotFoundException cuando el usuario no existe")
    void testFindBySupabaseId_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(siuUserRepository.findBySupabaseUserId(nonExistentId))
            .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> siuUserService.findBySupabaseId(nonExistentId));
        assertTrue(exception.getMessage().contains("Usuario no encontrado con supabaseUserId"));
        verify(siuUserRepository, times(1)).findBySupabaseUserId(nonExistentId);
    }

    @Test
    @DisplayName("Debería retornar usuario con todos los campos correctos")
    void testFindBySupabaseId_AllFields() {
        // Given
        UserSIU userWithAllFields = new UserSIU();
        userWithAllFields.setIdUserSIU(2);
        userWithAllFields.setName("Complete User");
        userWithAllFields.setPhotoUrl("https://example.com/complete-photo.jpg");
        userWithAllFields.setSupabaseUserId(testSupabaseUserId);

        when(siuUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(userWithAllFields));

        // When
        UserSIU result = siuUserService.findBySupabaseId(testSupabaseUserId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getIdUserSIU());
        assertEquals("Complete User", result.getName());
        assertEquals("https://example.com/complete-photo.jpg", result.getPhotoUrl());
        assertEquals(testSupabaseUserId, result.getSupabaseUserId());
    }

    @Test
    @DisplayName("Debería manejar usuario con photoUrl nulo")
    void testFindBySupabaseId_NullPhotoUrl() {
        // Given
        UserSIU userWithoutPhoto = new UserSIU();
        userWithoutPhoto.setIdUserSIU(3);
        userWithoutPhoto.setName("User Without Photo");
        userWithoutPhoto.setPhotoUrl(null);
        userWithoutPhoto.setSupabaseUserId(testSupabaseUserId);

        when(siuUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(userWithoutPhoto));

        // When
        UserSIU result = siuUserService.findBySupabaseId(testSupabaseUserId);

        // Then
        assertNotNull(result);
        assertNull(result.getPhotoUrl());
        assertEquals("User Without Photo", result.getName());
    }

    @Test
    @DisplayName("Debería manejar múltiples búsquedas del mismo usuario")
    void testFindBySupabaseId_MultipleCalls() {
        // Given
        when(siuUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUserSIU));

        // When
        UserSIU result1 = siuUserService.findBySupabaseId(testSupabaseUserId);
        UserSIU result2 = siuUserService.findBySupabaseId(testSupabaseUserId);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getIdUserSIU(), result2.getIdUserSIU());
        verify(siuUserRepository, times(2)).findBySupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería lanzar excepción con mensaje descriptivo")
    void testFindBySupabaseId_ExceptionMessage() {
        // Given
        UUID testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(siuUserRepository.findBySupabaseUserId(testId))
            .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> siuUserService.findBySupabaseId(testId));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        assertTrue(exception.getMessage().contains(testId.toString()));
    }

    @Test
    @DisplayName("Debería manejar diferentes UUIDs correctamente")
    void testFindBySupabaseId_DifferentUUIDs() {
        // Given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        
        UserSIU user1 = new UserSIU();
        user1.setIdUserSIU(1);
        user1.setName("User 1");
        user1.setSupabaseUserId(uuid1);

        UserSIU user2 = new UserSIU();
        user2.setIdUserSIU(2);
        user2.setName("User 2");
        user2.setSupabaseUserId(uuid2);

        when(siuUserRepository.findBySupabaseUserId(uuid1))
            .thenReturn(Optional.of(user1));
        when(siuUserRepository.findBySupabaseUserId(uuid2))
            .thenReturn(Optional.of(user2));

        // When
        UserSIU result1 = siuUserService.findBySupabaseId(uuid1);
        UserSIU result2 = siuUserService.findBySupabaseId(uuid2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("User 1", result1.getName());
        assertEquals("User 2", result2.getName());
        assertNotEquals(result1.getIdUserSIU(), result2.getIdUserSIU());
    }
}

