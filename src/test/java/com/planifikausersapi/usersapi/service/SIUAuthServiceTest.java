package com.planifikausersapi.usersapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriBuilder;

import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.model.UserSIU;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para SIUAuthService")
class SIUAuthServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private UserService userService;

    @Mock
    private SIUUserService siuUserService;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    private RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    private SIUAuthService siuAuthService;

    // Usuario SIU proporcionado para pruebas
    private static final UUID SIU_USER_SUPABASE_ID = UUID.fromString("05660f12-cb19-4b14-814d-b3fc8c632d5e");
    private static final Integer SIU_USER_ID = 48;
    private static final String SIU_USER_NAME = "Sara Méndez";
    private static final String SIU_USER_PHOTO_URL = " https://images.pexels.com/photos/3727463/pexels-photo-3727463.jpeg";
    private static final Integer SIU_USER_STATUS = 1;
    private static final Integer SIU_USER_TYPE = 3;

    private UserPlanifika testUserPlanifika;
    private UserSIU testUserSIU;
    private String testEmail;
    private String testPassword;
    private String serviceKey;

    @BeforeEach
    void setUp() {
        testEmail = "sara.mendez@example.com";
        testPassword = "password123";
        serviceKey = "test-service-key-12345";

        // Configurar usuario Planifika basado en el usuario SIU proporcionado
        testUserPlanifika = new UserPlanifika();
        testUserPlanifika.setIdUser(SIU_USER_ID);
        testUserPlanifika.setName(SIU_USER_NAME);
        testUserPlanifika.setPhotoUrl(SIU_USER_PHOTO_URL.trim());
        testUserPlanifika.setSupabaseUserId(SIU_USER_SUPABASE_ID);
        testUserPlanifika.setIdUserStatus(SIU_USER_STATUS);
        testUserPlanifika.setIdUserType(SIU_USER_TYPE);
        testUserPlanifika.setIdOrganization(null);

        // Configurar usuario SIU
        testUserSIU = new UserSIU();
        testUserSIU.setIdUserSIU(16);
        testUserSIU.setName(SIU_USER_NAME);
        testUserSIU.setPhotoUrl(SIU_USER_PHOTO_URL.trim());
        testUserSIU.setSupabaseUserId(SIU_USER_SUPABASE_ID);

        // Configurar WebClient mocks
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // Construir SIUAuthService manualmente
        siuAuthService = new SIUAuthService("http://test.supabase.url", "test-anon-key", serviceKey, webClientBuilder, userService, siuUserService);
    }

    @Test
    @DisplayName("Debería realizar signIn exitosamente")
    void testSignIn_Success() {
        // Given
        Map<String, Object> authResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", SIU_USER_SUPABASE_ID.toString());
        userMap.put("email", testEmail);
        authResponse.put("user", userMap);
        authResponse.put("access_token", "test-access-token");
        authResponse.put("token_type", "bearer");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.just(authResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());

        // When
        Mono<Map<String, Object>> result = siuAuthService.signIn(testEmail, testPassword);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("user"));
                assertTrue(response.containsKey("access_token"));
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) response.get("user");
                assertEquals(SIU_USER_SUPABASE_ID.toString(), user.get("id"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error en signIn cuando las credenciales son inválidas")
    void testSignIn_InvalidCredentials() {
        // Given
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.error(new RuntimeException("Error durante la petición de autenticación")))
            .when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());

        // When
        Mono<Map<String, Object>> result = siuAuthService.signIn(testEmail, "wrong-password");

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(error -> 
                error instanceof RuntimeException && 
                error.getMessage().contains("Error durante la petición de autenticación"))
            .verify();
    }

    @Test
    @DisplayName("Debería realizar externalLogin exitosamente cuando el usuario existe")
    void testExternalLogin_UserExists() {
        // Given
        Map<String, Object> authResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", SIU_USER_SUPABASE_ID.toString());
        userMap.put("email", testEmail);
        authResponse.put("user", userMap);
        authResponse.put("access_token", "test-access-token");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.just(authResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        when(userService.findBySupabaseId(SIU_USER_SUPABASE_ID))
            .thenReturn(testUserPlanifika);
        when(userService.save(any(UserPlanifika.class))).thenReturn(testUserPlanifika);

        // Usar reflection para establecer el serviceKey
        try {
            java.lang.reflect.Field field = SIUAuthService.class.getDeclaredField("serviceKey");
            field.setAccessible(true);
            field.set(siuAuthService, serviceKey);
        } catch (Exception e) {
            fail("No se pudo establecer el serviceKey: " + e.getMessage());
        }

        // When
        Mono<Map<String, Object>> result = siuAuthService.externalLogin(testEmail, testPassword);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("user"));
                assertTrue(response.containsKey("access_token"));
                // Para estudiantes (idUserType=3), debe usar SERVICE_KEY
                assertEquals(serviceKey, response.get("access_token"));
                UserPlanifika user = (UserPlanifika) response.get("user");
                assertEquals(SIU_USER_ID, user.getIdUser());
                assertEquals(SIU_USER_NAME, user.getName());
                assertEquals(SIU_USER_TYPE, user.getIdUserType());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería crear nuevo usuario cuando no existe en externalLogin")
    void testExternalLogin_UserNotExists_CreatesNew() {
        // Given
        Map<String, Object> authResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", SIU_USER_SUPABASE_ID.toString());
        userMap.put("email", testEmail);
        authResponse.put("user", userMap);
        authResponse.put("access_token", "test-access-token");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.just(authResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        when(userService.findBySupabaseId(SIU_USER_SUPABASE_ID))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("Usuario no encontrado"));
        when(siuUserService.findBySupabaseId(SIU_USER_SUPABASE_ID))
            .thenReturn(testUserSIU);
        when(userService.save(any(UserPlanifika.class))).thenAnswer(invocation -> {
            UserPlanifika user = invocation.getArgument(0);
            user.setIdUser(SIU_USER_ID);
            return user;
        });

        // Usar reflection para establecer el serviceKey
        try {
            java.lang.reflect.Field field = SIUAuthService.class.getDeclaredField("serviceKey");
            field.setAccessible(true);
            field.set(siuAuthService, serviceKey);
        } catch (Exception e) {
            fail("No se pudo establecer el serviceKey: " + e.getMessage());
        }

        // When
        Mono<Map<String, Object>> result = siuAuthService.externalLogin(testEmail, testPassword);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("user"));
                assertTrue(response.containsKey("access_token"));
                // Para estudiantes nuevos (idUserType=3), debe usar SERVICE_KEY
                assertEquals(serviceKey, response.get("access_token"));
                UserPlanifika user = (UserPlanifika) response.get("user");
                assertEquals(SIU_USER_ID, user.getIdUser());
                assertEquals(SIU_USER_NAME, user.getName());
                assertEquals(SIU_USER_TYPE, user.getIdUserType());
                assertEquals(SIU_USER_STATUS, user.getIdUserStatus());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería actualizar usuario cuando tiene datos incorrectos")
    void testExternalLogin_UserNeedsUpdate() {
        // Given
        Map<String, Object> authResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", SIU_USER_SUPABASE_ID.toString());
        userMap.put("email", testEmail);
        authResponse.put("user", userMap);
        authResponse.put("access_token", "test-access-token");

        // Usuario con datos incorrectos que necesitan actualización
        UserPlanifika userWithWrongData = new UserPlanifika();
        userWithWrongData.setIdUser(SIU_USER_ID);
        userWithWrongData.setName(SIU_USER_NAME);
        userWithWrongData.setSupabaseUserId(SIU_USER_SUPABASE_ID);
        userWithWrongData.setIdUserType(1); // Tipo incorrecto (debe ser 3)
        userWithWrongData.setIdUserStatus(0); // Estado incorrecto (debe ser 1)

        UserPlanifika updatedUser = new UserPlanifika();
        updatedUser.setIdUser(SIU_USER_ID);
        updatedUser.setName(SIU_USER_NAME);
        updatedUser.setSupabaseUserId(SIU_USER_SUPABASE_ID);
        updatedUser.setIdUserType(3); // Corregido
        updatedUser.setIdUserStatus(1); // Corregido

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.just(authResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        when(userService.findBySupabaseId(SIU_USER_SUPABASE_ID))
            .thenReturn(userWithWrongData);
        when(userService.save(any(UserPlanifika.class))).thenReturn(updatedUser);

        // Usar reflection para establecer el serviceKey
        try {
            java.lang.reflect.Field field = SIUAuthService.class.getDeclaredField("serviceKey");
            field.setAccessible(true);
            field.set(siuAuthService, serviceKey);
        } catch (Exception e) {
            fail("No se pudo establecer el serviceKey: " + e.getMessage());
        }

        // When
        Mono<Map<String, Object>> result = siuAuthService.externalLogin(testEmail, testPassword);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("user"));
                assertTrue(response.containsKey("access_token"));
                UserPlanifika user = (UserPlanifika) response.get("user");
                assertEquals(3, user.getIdUserType()); // Debe estar corregido
                assertEquals(1, user.getIdUserStatus()); // Debe estar corregido
                verify(userService, times(1)).save(any(UserPlanifika.class));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería usar token de usuario cuando idUserType no es 3")
    void testExternalLogin_NonStudentUser() {
        // Given
        Map<String, Object> authResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", SIU_USER_SUPABASE_ID.toString());
        userMap.put("email", testEmail);
        authResponse.put("user", userMap);
        authResponse.put("access_token", "user-access-token");

        UserPlanifika nonStudentUser = new UserPlanifika();
        nonStudentUser.setIdUser(SIU_USER_ID);
        nonStudentUser.setName(SIU_USER_NAME);
        nonStudentUser.setSupabaseUserId(SIU_USER_SUPABASE_ID);
        nonStudentUser.setIdUserType(1); // No es estudiante
        nonStudentUser.setIdUserStatus(1);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ArgumentMatchers.<Function<UriBuilder, URI>>any());
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.just(authResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        when(userService.findBySupabaseId(SIU_USER_SUPABASE_ID))
            .thenReturn(nonStudentUser);

        // Usar reflection para establecer el serviceKey
        try {
            java.lang.reflect.Field field = SIUAuthService.class.getDeclaredField("serviceKey");
            field.setAccessible(true);
            field.set(siuAuthService, serviceKey);
        } catch (Exception e) {
            fail("No se pudo establecer el serviceKey: " + e.getMessage());
        }

        // When
        Mono<Map<String, Object>> result = siuAuthService.externalLogin(testEmail, testPassword);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("user"));
                assertTrue(response.containsKey("access_token"));
                // Para usuarios no estudiantes, debe usar el token del usuario
                assertEquals("user-access-token", response.get("access_token"));
            })
            .verifyComplete();
    }
}

