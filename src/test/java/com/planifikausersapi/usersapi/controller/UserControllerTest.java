package com.planifikausersapi.usersapi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.planifikausersapi.usersapi.enums.UserStatusEnum;
import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.service.UserService;
import com.planifikausersapi.usersapi.utils.ErrorResponse;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para UserController")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserPlanifika testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserPlanifika();
        testUser.setIdUser(1);
        testUser.setName("Test User");
        testUser.setPhotoUrl("https://example.com/photo.jpg");
        testUser.setSupabaseUserId(UUID.randomUUID());
        testUser.setIdUserStatus(1);
        testUser.setIdUserType(1);
        testUser.setIdOrganization(100);
    }

    @Test
    @DisplayName("Debería retornar todos los usuarios exitosamente")
    void testGetAllUsers_Success() {
        // Given
        List<UserPlanifika> users = Arrays.asList(testUser);
        when(userService.findAll()).thenReturn(users);

        // When
        ResponseEntity<Object> response = userController.getAllUsers();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería retornar error cuando getAllUsers falla")
    void testGetAllUsers_Error() {
        // Given
        when(userService.findAll()).thenThrow(new RuntimeException("Error"));

        // When
        ResponseEntity<Object> response = userController.getAllUsers();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería retornar usuario por ID exitosamente")
    void testGetUserById_Success() {
        // Given
        when(userService.findById(1)).thenReturn(testUser);

        // When
        ResponseEntity<Object> response = userController.getUserById(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debería retornar NOT_FOUND cuando el usuario no existe")
    void testGetUserById_NotFound() {
        // Given
        when(userService.findById(999)).thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        // When
        ResponseEntity<Object> response = userController.getUserById(999);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        verify(userService, times(1)).findById(999);
    }

    @Test
    @DisplayName("Debería retornar error cuando getUserById falla")
    void testGetUserById_Error() {
        // Given
        when(userService.findById(1)).thenThrow(new RuntimeException("Error"));

        // When
        ResponseEntity<Object> response = userController.getUserById(1);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    @DisplayName("Debería crear usuario exitosamente")
    void testCreateUser_Success() {
        // Given
        UserPlanifika newUser = new UserPlanifika();
        newUser.setName("New User");
        when(userService.save(any(UserPlanifika.class))).thenReturn(testUser);

        // When
        ResponseEntity<Object> response = userController.createUser(newUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).save(newUser);
    }

    @Test
    @DisplayName("Debería retornar error cuando createUser falla")
    void testCreateUser_Error() {
        // Given
        UserPlanifika newUser = new UserPlanifika();
        when(userService.save(any(UserPlanifika.class))).thenThrow(new RuntimeException("Error"));

        // When
        ResponseEntity<Object> response = userController.createUser(newUser);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    @DisplayName("Debería actualizar usuario exitosamente")
    void testUpdateUser_Success() {
        // Given
        when(userService.update(any(UserPlanifika.class))).thenReturn(testUser);

        // When
        ResponseEntity<Object> response = userController.updateUser(1, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, testUser.getIdUser());
        verify(userService, times(1)).update(testUser);
    }

    @Test
    @DisplayName("Debería retornar NOT_FOUND al actualizar usuario inexistente")
    void testUpdateUser_NotFound() {
        // Given
        when(userService.update(any(UserPlanifika.class)))
            .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        // When
        ResponseEntity<Object> response = userController.updateUser(999, testUser);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    @DisplayName("Debería retornar error cuando updateUser falla")
    void testUpdateUser_Error() {
        // Given
        when(userService.update(any(UserPlanifika.class))).thenThrow(new RuntimeException("Error"));

        // When
        ResponseEntity<Object> response = userController.updateUser(1, testUser);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debería actualizar parcialmente usuario exitosamente")
    void testPatchUpdateUser_Success() {
        // Given
        when(userService.patchUpdate(any(UserPlanifika.class))).thenReturn(testUser);

        // When
        ResponseEntity<Object> response = userController.patchUpdateUser(1, testUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, testUser.getIdUser());
        verify(userService, times(1)).patchUpdate(testUser);
    }

    @Test
    @DisplayName("Debería retornar NOT_FOUND al actualizar parcialmente usuario inexistente")
    void testPatchUpdateUser_NotFound() {
        // Given
        when(userService.patchUpdate(any(UserPlanifika.class)))
            .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        // When
        ResponseEntity<Object> response = userController.patchUpdateUser(999, testUser);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Debería actualizar estado de usuario exitosamente")
    void testUpdateUserStatus_Success() {
        // Given
        when(userService.updateStatus(1, "ACTIVE")).thenReturn(testUser);

        // When
        ResponseEntity<Object> response = userController.updateUserStatus(1, "ACTIVE");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).updateStatus(1, "ACTIVE");
    }

    @Test
    @DisplayName("Debería retornar NOT_FOUND al actualizar estado de usuario inexistente")
    void testUpdateUserStatus_NotFound() {
        // Given
        when(userService.updateStatus(999, "ACTIVE"))
            .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        // When
        ResponseEntity<Object> response = userController.updateUserStatus(999, "ACTIVE");

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Debería retornar error cuando updateUserStatus falla")
    void testUpdateUserStatus_Error() {
        // Given
        when(userService.updateStatus(1, "INVALID"))
            .thenThrow(new IllegalArgumentException("Estado inválido"));

        // When
        ResponseEntity<Object> response = userController.updateUserStatus(1, "INVALID");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debería actualizar organización exitosamente")
    void testUpdateOrganization_Success() {
        // Given
        when(userService.updateOrganization(1, 200)).thenReturn(testUser);

        // When
        ResponseEntity<UserPlanifika> response = userController.updateOrganization(1, 200, "Bearer token");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).updateOrganization(1, 200);
    }

    @Test
    @DisplayName("Debería retornar error cuando updateOrganization falla")
    void testUpdateOrganization_Error() {
        // Given
        when(userService.updateOrganization(1, 200))
            .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        // When
        ResponseEntity<UserPlanifika> response = userController.updateOrganization(1, 200, "Bearer token");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debería eliminar usuario exitosamente")
    void testDeleteUser_Success() {
        // Given
        testUser.setIdUserStatus(UserStatusEnum.DELETED.getId());
        when(userService.delete(1)).thenReturn(testUser);

        // When
        ResponseEntity<Object> response = userController.deleteUser(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).delete(1);
    }

    @Test
    @DisplayName("Debería retornar NOT_FOUND al eliminar usuario inexistente")
    void testDeleteUser_NotFound() {
        // Given
        when(userService.delete(999))
            .thenThrow(new EntityNotFoundException("Usuario no encontrado"));

        // When
        ResponseEntity<Object> response = userController.deleteUser(999);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Debería retornar error cuando deleteUser falla")
    void testDeleteUser_Error() {
        // Given
        when(userService.delete(1)).thenThrow(new RuntimeException("Error"));

        // When
        ResponseEntity<Object> response = userController.deleteUser(1);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

