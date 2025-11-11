package com.planifikausersapi.usersapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planifikausersapi.usersapi.enums.UserStatusEnum;
import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.repository.planifika.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserPlanifika testUser;
    private UUID testSupabaseUserId;

    @BeforeEach
    void setUp() {
        testSupabaseUserId = UUID.randomUUID();
        testUser = new UserPlanifika();
        testUser.setIdUser(1);
        testUser.setName("Test User");
        testUser.setPhotoUrl("https://example.com/photo.jpg");
        testUser.setSupabaseUserId(testSupabaseUserId);
        testUser.setIdUserStatus(1);
        testUser.setIdUserType(1);
        testUser.setIdOrganization(100);
    }

    @Test
    @DisplayName("Debería retornar todos los usuarios")
    void testFindAll() {
        // Given
        List<UserPlanifika> users = Arrays.asList(testUser, createUser(2, "User 2"));
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserPlanifika> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería retornar usuario por ID cuando existe")
    void testFindById_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // When
        UserPlanifika result = userService.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getIdUser());
        assertEquals("Test User", result.getName());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debería lanzar EntityNotFoundException cuando el usuario no existe")
    void testFindById_NotFound() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> userService.findById(999));
        assertTrue(exception.getMessage().contains("Usuario no encontrado con id: 999"));
        verify(userRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Debería retornar usuario por SupabaseUserId cuando existe")
    void testFindBySupabaseId_Success() {
        // Given
        when(userRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUser));

        // When
        UserPlanifika result = userService.findBySupabaseId(testSupabaseUserId);

        // Then
        assertNotNull(result);
        assertEquals(testSupabaseUserId, result.getSupabaseUserId());
        verify(userRepository, times(1)).findBySupabaseUserId(testSupabaseUserId);
    }

    @Test
    @DisplayName("Debería lanzar EntityNotFoundException cuando el SupabaseUserId no existe")
    void testFindBySupabaseId_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findBySupabaseUserId(nonExistentId))
            .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> userService.findBySupabaseId(nonExistentId));
        assertTrue(exception.getMessage().contains("Usuario no encontrado con supabaseUserId"));
        verify(userRepository, times(1)).findBySupabaseUserId(nonExistentId);
    }

    @Test
    @DisplayName("Debería guardar un nuevo usuario")
    void testSave() {
        // Given
        UserPlanifika newUser = new UserPlanifika();
        newUser.setName("New User");
        newUser.setSupabaseUserId(UUID.randomUUID());
        
        when(userRepository.save(any(UserPlanifika.class))).thenReturn(testUser);

        // When
        UserPlanifika result = userService.save(newUser);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("Debería actualizar un usuario existente")
    void testUpdate_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenReturn(testUser);

        // When
        UserPlanifika result = userService.update(testUser);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar usuario inexistente")
    void testUpdate_NotFound() {
        // Given
        UserPlanifika nonExistentUser = new UserPlanifika();
        nonExistentUser.setIdUser(999);
        nonExistentUser.setName("Non Existent User");
        
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, 
            () -> userService.update(nonExistentUser));
        verify(userRepository, times(1)).findById(999);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar parcialmente un usuario")
    void testPatchUpdate_PartialUpdate() {
        // Given
        UserPlanifika updatedUser = new UserPlanifika();
        updatedUser.setIdUser(1);
        updatedUser.setName("Updated Name");
        updatedUser.setPhotoUrl("https://example.com/new-photo.jpg");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenAnswer(invocation -> {
            UserPlanifika saved = invocation.getArgument(0);
            return saved;
        });

        // When
        UserPlanifika result = userService.patchUpdate(updatedUser);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("https://example.com/new-photo.jpg", result.getPhotoUrl());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserPlanifika.class));
    }

    @Test
    @DisplayName("Debería actualizar solo el nombre en patchUpdate")
    void testPatchUpdate_OnlyName() {
        // Given
        UserPlanifika updatedUser = new UserPlanifika();
        updatedUser.setIdUser(1);
        updatedUser.setName("New Name Only");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));

        // When
        UserPlanifika result = userService.patchUpdate(updatedUser);

        // Then
        assertEquals("New Name Only", result.getName());
        assertEquals(testUser.getPhotoUrl(), result.getPhotoUrl()); // No cambió
    }

    @Test
    @DisplayName("Debería actualizar el estado del usuario")
    void testUpdateStatus_Success() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));

        // When
        UserPlanifika result = userService.updateStatus(1, "ACTIVE");

        // Then
        assertNotNull(result);
        assertEquals(UserStatusEnum.ACTIVE.getId(), result.getIdUserStatus());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserPlanifika.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción con estado inválido")
    void testUpdateStatus_InvalidStatus() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> userService.updateStatus(1, "INVALID_STATUS"));
        assertTrue(exception.getMessage().contains("Estado inválido"));
    }

    @Test
    @DisplayName("Debería marcar usuario como eliminado")
    void testDelete() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));

        // When
        UserPlanifika result = userService.delete(1);

        // Then
        assertNotNull(result);
        assertEquals(UserStatusEnum.DELETED.getId(), result.getIdUserStatus());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserPlanifika.class));
    }

    @Test
    @DisplayName("Debería actualizar la organización del usuario")
    void testUpdateOrganization() {
        // Given
        Integer newOrganizationId = 200;
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));

        // When
        UserPlanifika result = userService.updateOrganization(1, newOrganizationId);

        // Then
        assertNotNull(result);
        assertEquals(newOrganizationId, result.getIdOrganization());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserPlanifika.class));
    }

    @Test
    @DisplayName("Debería manejar null en patchUpdate sin errores")
    void testPatchUpdate_WithNulls() {
        // Given
        UserPlanifika updatedUser = new UserPlanifika();
        updatedUser.setIdUser(1);
        // No se establecen otros campos (null)

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));

        // When
        UserPlanifika result = userService.patchUpdate(updatedUser);

        // Then
        assertNotNull(result);
        // Los valores originales se mantienen
        assertEquals(testUser.getName(), result.getName());
        verify(userRepository, times(1)).save(any(UserPlanifika.class));
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay usuarios")
    void testFindAll_EmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<UserPlanifika> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    // Helper method
    private UserPlanifika createUser(Integer id, String name) {
        UserPlanifika user = new UserPlanifika();
        user.setIdUser(id);
        user.setName(name);
        user.setSupabaseUserId(UUID.randomUUID());
        return user;
    }
}

