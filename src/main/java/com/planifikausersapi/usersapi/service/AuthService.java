package com.planifikausersapi.usersapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.planifikausersapi.usersapi.repository.UserRepository;
import com.planifikausersapi.usersapi.dto.DtoUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String anonKey;
    private final UserRepository userRepository;

    public AuthService(@Value("${supabase.url}") String supabaseUrl,
                       @Value("${supabase.anon.key}") String anonKey,
                       WebClient.Builder webClientBuilder,
                       UserRepository userRepository) {
        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
        this.userRepository = userRepository;
        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl)
                .defaultHeader("apiKey", anonKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                .build();
    }

public Mono<Map<String, Object>> signUp(String name, String email, String password, String photoUrl, Integer userRole) {

    // 1. DEFINE GENERIC TYPES EXPLICITLY to resolve 'Cannot infer type' errors
    ParameterizedTypeReference<Map<String, Object>> mapType = 
        new ParameterizedTypeReference<Map<String, Object>>() {};
    
    // This is the CRITICAL type for the DB response: a List of Maps
    ParameterizedTypeReference<List<Map<String, Object>>> listMapType =
        new ParameterizedTypeReference<List<Map<String, Object>>>() {};

    Map<String, Object> authPayload = Map.of(
            "email", email,
            "password", password
    );

    // 1. Supabase Auth Call
    return webClient.post()
            .uri("/auth/v1/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(authPayload)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .defaultIfEmpty("[No response body]")
                    .flatMap(errorBody -> Mono.error(new RuntimeException("Supabase Auth Error (" + clientResponse.statusCode() + "): " + errorBody)))
            )
            .bodyToMono(mapType) // Use explicit type for Mono<Map>
            .flatMap(response -> {
                System.out.println("📥 Respuesta Supabase Auth: " + response);

                Object idCandidate = null;
                if (response.containsKey("user")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userMap = (Map<String, Object>) response.get("user");
                    idCandidate = userMap != null ? userMap.get("id") : null;
                } else if (response.containsKey("id")) {
                    idCandidate = response.get("id");
                }

                if (idCandidate == null) {
                    return Mono.error(new RuntimeException("No se encontró el ID del usuario en la respuesta de Supabase: " + response));
                }

                // Safe UUID Parsing
                UUID supabaseUserId;
                try {
                    supabaseUserId = UUID.fromString(idCandidate.toString());
                } catch (IllegalArgumentException e) {
                    return Mono.error(new RuntimeException("Error al parsear ID de Supabase: " + idCandidate, e));
                }

                // Prepare DB Insertion Payload (Using HashMap for null support)
                Map<String, Object> newUser = new HashMap<>();
                newUser.put("supabaseuserid", supabaseUserId);
                newUser.put("name", name);
                newUser.put("photourl", photoUrl);
                newUser.put("iduserstatus", 1);
                newUser.put("idusertype", userRole != null ? userRole : 1);
                newUser.put("idorganization", null); 

                // 2. Supabase DB (PostgREST) Call
                return webClient.post()
                        .uri("/rest/v1/userplanifika")
                        .header("Prefer", "return=representation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(newUser)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("[No response body]")
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Supabase DB Error (" + clientResponse.statusCode() + "): " + errorBody)))
                        )
                        // 💥 FIX: Expect a List of Maps (listMapType) to handle the JSON array.
                        .bodyToMono(listMapType) 
                        .map(dbResponseList -> {
                            if (dbResponseList.isEmpty()) {
                                throw new RuntimeException("Inserción en DB Supabase exitosa, pero no devolvió datos.");
                            }
                            // Extract the first (and only) item from the list for the final response.
                            return Map.<String, Object>of(
                                    "auth", response,
                                    "db", dbResponseList.get(0)
                            );
                        });
            })
            .onErrorResume(e -> {
                e.printStackTrace();
                String errorMessage = e.getMessage() != null ? e.getMessage() : "A truly unknown error occurred, check server logs for stack trace.";
                return Mono.just(Map.of("error", "Error en signUp: " + errorMessage));
            });
}


    public Mono<Map<String, Object>> signIn(String email, String password) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/auth/v1/token")
                        .queryParam("grant_type", "password")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }


    public Mono<Map<String, Object>> getUser(String accessToken) {
        return webClient.get()
                .uri("/auth/v1/user")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Obtiene la información completa del usuario combinando datos de Supabase Auth 
     * con datos de la base de datos de la aplicación
     */
    public Mono<Map<String, Object>> getUserWithDatabaseInfo(String accessToken) {
        return getUser(accessToken)
                .flatMap((Map<String, Object> supabaseUser) -> {
                    try {
                        // Extraer el ID del usuario de Supabase
                        String supabaseUserIdStr = (String) supabaseUser.get("id");
                        if (supabaseUserIdStr == null) {
                            return Mono.error(new RuntimeException("No se encontró el ID del usuario en Supabase"));
                        }
                        
                        UUID supabaseUserId = UUID.fromString(supabaseUserIdStr);
                        
                        // Buscar el usuario en la base de datos de la aplicación
                        return userRepository.findBySupabaseUserId(supabaseUserId)
                                .map(dtoUser -> {
                                    // Crear respuesta combinada con datos de la BD
                                    Map<String, Object> response = new HashMap<>();
                                    response.put("userId", dtoUser.getIdUser());
                                    response.put("name", dtoUser.getName());
                                    response.put("email", supabaseUser.get("email")); // Email desde Supabase
                                    response.put("photoUrl", dtoUser.getPhotoUrl());
                                    response.put("userType", dtoUser.getIdUserType()); // CRÍTICO: Tipo de usuario
                                    response.put("idusertype", dtoUser.getIdUserType()); // CRÍTICO: ID del tipo de usuario
                                    response.put("iduserstatus", dtoUser.getIdUserStatus());
                                    response.put("idorganization", dtoUser.getIdOrganization());
                                    response.put("supabaseUserId", dtoUser.getSupabaseUserId());
                                    
                                    return response;
                                })
                                .map(Mono::just)
                                .orElse(Mono.error(new RuntimeException("Usuario no encontrado en la base de datos de la aplicación")));
                                
                    } catch (IllegalArgumentException e) {
                        return Mono.error(new RuntimeException("Error al parsear el ID de Supabase: " + e.getMessage()));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error al obtener información del usuario: " + e.getMessage()));
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Error en getUserWithDatabaseInfo: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }
}
