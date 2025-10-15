package com.planifikausersapi.usersapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String anonKey;

    public AuthService(@Value("${supabase.url}") String supabaseUrl,
                       @Value("${supabase.anon.key}") String anonKey,
                       WebClient.Builder webClientBuilder) {
        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl)
                .defaultHeader("apiKey", anonKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                .build();
    }

public Mono<Map<String, Object>> signUp(String name, String email, String password, String photoUrl) {

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
                newUser.put("idusertype", 1);
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


    public Mono<Map> signIn(String email, String password) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/auth/v1/token")
                        .queryParam("grant_type", "password")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(Map.class);
    }


    public Mono<Map> getUser(String accessToken) {
        return webClient.get()
                .uri("/auth/v1/user")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class);
    }
}
