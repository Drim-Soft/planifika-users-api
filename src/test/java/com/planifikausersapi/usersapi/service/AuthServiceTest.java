package com.planifikausersapi.usersapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.repository.planifika.UserRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para AuthService")
class AuthServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RequestBodySpec requestBodySpec;

    @Mock
    private RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private ResponseSpec responseSpec;

    @Mock
    private ClientResponse clientResponse;

    private AuthService authService;

    private UserPlanifika testUser;
    private UUID testSupabaseUserId;
    private String testEmail;
    private String testPassword;
    private String testName;
    private String testPhotoUrl;

    @BeforeEach
    void setUp() {
        testSupabaseUserId = UUID.randomUUID();
        testEmail = "test@example.com";
        testPassword = "password123";
        testName = "Test User";
        testPhotoUrl = "https://example.com/photo.jpg";

        testUser = new UserPlanifika();
        testUser.setIdUser(1);
        testUser.setName(testName);
        testUser.setPhotoUrl(testPhotoUrl);
        testUser.setSupabaseUserId(testSupabaseUserId);
        testUser.setIdUserStatus(1);
        testUser.setIdUserType(1);
        testUser.setIdOrganization(100);

        // Configurar WebClient mocks
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // Construir AuthService manualmente
        authService = new AuthService("http://test.supabase.url", "test-anon-key", webClientBuilder, userRepository);
    }

    @Test
    @DisplayName("Debería realizar signUp exitosamente")
    void testSignUp_Success() {
        // Given
        Integer userRole = 1;
        Map<String, Object> authResponse = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", testSupabaseUserId.toString());
        authResponse.put("user", userMap);

        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put("iduser", 1);
        dbUser.put("name", testName);
        dbUser.put("supabaseuserid", testSupabaseUserId.toString());
        List<Map<String, Object>> dbResponseList = List.of(dbUser);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.just(authResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        doReturn(Mono.just(dbResponseList)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any());

        // When
        Mono<Map<String, Object>> result = authService.signUp(testName, testEmail, testPassword, testPhotoUrl, userRole);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertFalse(response.containsKey("error"));
                assertTrue(response.containsKey("auth"));
                assertTrue(response.containsKey("db"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error en signUp cuando Supabase Auth falla")
    void testSignUp_AuthError() {
        // Given
        Integer userRole = 1;
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.error(new RuntimeException("Supabase Auth Error (400): Invalid credentials")))
            .when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());

        // When
        Mono<Map<String, Object>> result = authService.signUp(testName, testEmail, testPassword, testPhotoUrl, userRole);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("error"));
                assertTrue(response.get("error").toString().contains("Error en signUp"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería realizar signIn exitosamente")
    void testSignIn_Success() {
        // Given
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("access_token", "test-access-token");
        authResponse.put("token_type", "bearer");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ArgumentMatchers.<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>any());
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(authResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());

        // When
        Mono<Map<String, Object>> result = authService.signIn(testEmail, testPassword);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals("test-access-token", response.get("access_token"));
                assertEquals("bearer", response.get("token_type"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener usuario por access token")
    void testGetUser_Success() {
        // Given
        String accessToken = "test-access-token";
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", testSupabaseUserId.toString());
        userResponse.put("email", testEmail);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(ArgumentMatchers.<java.util.function.Consumer<org.springframework.http.HttpHeaders>>any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(userResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());

        // When
        Mono<Map<String, Object>> result = authService.getUser(accessToken);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(testSupabaseUserId.toString(), response.get("id"));
                assertEquals(testEmail, response.get("email"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener usuario con información de base de datos")
    void testGetUserWithDatabaseInfo_Success() {
        // Given
        String accessToken = "test-access-token";
        Map<String, Object> supabaseUser = new HashMap<>();
        supabaseUser.put("id", testSupabaseUserId.toString());
        supabaseUser.put("email", testEmail);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(ArgumentMatchers.<java.util.function.Consumer<org.springframework.http.HttpHeaders>>any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(supabaseUser)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        when(userRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUser));

        // When
        Mono<Map<String, Object>> result = authService.getUserWithDatabaseInfo(accessToken);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(testUser.getIdUser(), response.get("userId"));
                assertEquals(testUser.getName(), response.get("name"));
                assertEquals(testEmail, response.get("email"));
                assertEquals(testUser.getPhotoUrl(), response.get("photoUrl"));
                assertEquals(testUser.getIdUserType(), response.get("userType"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería lanzar error cuando usuario no existe en base de datos")
    void testGetUserWithDatabaseInfo_UserNotFound() {
        // Given
        String accessToken = "test-access-token";
        Map<String, Object> supabaseUser = new HashMap<>();
        supabaseUser.put("id", testSupabaseUserId.toString());
        supabaseUser.put("email", testEmail);

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(ArgumentMatchers.<java.util.function.Consumer<org.springframework.http.HttpHeaders>>any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(Mono.just(supabaseUser)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        when(userRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.empty());

        // When
        Mono<Map<String, Object>> result = authService.getUserWithDatabaseInfo(accessToken);

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(error -> 
                error instanceof RuntimeException && 
                error.getMessage().contains("Usuario no encontrado en la base de datos"))
            .verify();
    }

    @Test
    @DisplayName("Debería actualizar perfil exitosamente")
    void testUpdateProfile_Success() {
        // Given
        String accessToken = "test-access-token";
        String newName = "Updated Name";
        String newPhotoUrl = "https://example.com/new-photo.jpg";
        Map<String, Object> supabaseUser = new HashMap<>();
        supabaseUser.put("id", testSupabaseUserId.toString());
        supabaseUser.put("email", testEmail);

        Map<String, Object> authUpdateResponse = new HashMap<>();
        authUpdateResponse.put("id", testSupabaseUserId.toString());

        doReturn(requestHeadersUriSpec).when(webClient).get();
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(ArgumentMatchers.<java.util.function.Consumer<org.springframework.http.HttpHeaders>>any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        doReturn(Mono.just(supabaseUser)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        doReturn(Mono.just(authUpdateResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        when(userRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserPlanifika.class))).thenAnswer(invocation -> {
            UserPlanifika user = invocation.getArgument(0);
            user.setName(newName);
            user.setPhotoUrl(newPhotoUrl);
            return user;
        });

        // When
        Mono<Map<String, Object>> result = authService.updateProfile(accessToken, newName, null, newPhotoUrl);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("auth"));
                assertTrue(response.containsKey("db"));
                assertTrue(response.containsKey("supabaseUserId"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debería lanzar error cuando no se proporcionan campos para actualizar")
    void testUpdateProfile_NoFields() {
        // Given
        String accessToken = "test-access-token";

        // When
        Mono<Map<String, Object>> result = authService.updateProfile(accessToken, null, null, null);

        // Then
        StepVerifier.create(result)
            .expectErrorMatches(error -> 
                error instanceof IllegalArgumentException && 
                error.getMessage().contains("Debe proporcionar"))
            .verify();
    }

    @Test
    @DisplayName("Debería actualizar solo la contraseña")
    void testUpdateProfile_PasswordOnly() {
        // Given
        String accessToken = "test-access-token";
        String newPassword = "newPassword123";
        Map<String, Object> supabaseUser = new HashMap<>();
        supabaseUser.put("id", testSupabaseUserId.toString());
        supabaseUser.put("email", testEmail);

        Map<String, Object> authUpdateResponse = new HashMap<>();
        authUpdateResponse.put("id", testSupabaseUserId.toString());

        doReturn(requestHeadersUriSpec).when(webClient).get();
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(ArgumentMatchers.<java.util.function.Consumer<org.springframework.http.HttpHeaders>>any());
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(org.springframework.http.MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        doReturn(Mono.just(supabaseUser)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());
        doReturn(Mono.just(authUpdateResponse)).when(responseSpec).bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any());

        // When
        Mono<Map<String, Object>> result = authService.updateProfile(accessToken, null, newPassword, null);

        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.containsKey("auth"));
                assertTrue(response.containsKey("db"));
            })
            .verifyComplete();
    }
}

