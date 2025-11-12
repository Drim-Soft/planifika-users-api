package com.planifikausersapi.usersapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.enums.UserStatusEnum;
import com.planifikausersapi.usersapi.repository.planifika.UserRepository;

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

    public Mono<Map<String, Object>> signUp(String name, String email, String password, String photoUrl,
            Integer userRole, Integer organizationId) {

        // 1. DEFINE GENERIC TYPES EXPLICITLY to resolve 'Cannot infer type' errors
        ParameterizedTypeReference<Map<String, Object>> mapType = new ParameterizedTypeReference<Map<String, Object>>() {
        };

        // This is the CRITICAL type for the DB response: a List of Maps
        ParameterizedTypeReference<List<Map<String, Object>>> listMapType = new ParameterizedTypeReference<List<Map<String, Object>>>() {
        };

        Map<String, Object> authPayload = Map.of(
                "email", email,
                "password", password);

        // 1. Supabase Auth Call
        return webClient.post()
                .uri("/auth/v1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authPayload)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("[No response body]")
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "Supabase Auth Error (" + clientResponse.statusCode() + "): " + errorBody))))
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
                        return Mono.error(new RuntimeException(
                                "No se encontró el ID del usuario en la respuesta de Supabase: " + response));
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
                    newUser.put("idorganization", organizationId); // Usar el organizationId recibido (puede ser null)

                    // 2. Supabase DB (PostgREST) Call
                    return webClient.post()
                            .uri("/rest/v1/userplanifika")
                            .header("Prefer", "return=representation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(newUser)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                    clientResponse -> clientResponse.bodyToMono(String.class)
                                            .defaultIfEmpty("[No response body]")
                                            .flatMap(errorBody -> Mono.error(new RuntimeException("Supabase DB Error ("
                                                    + clientResponse.statusCode() + "): " + errorBody))))
                            // 💥 FIX: Expect a List of Maps (listMapType) to handle the JSON array.
                            .bodyToMono(listMapType)
                            .map(dbResponseList -> {
                                if (dbResponseList.isEmpty()) {
                                    throw new RuntimeException(
                                            "Inserción en DB Supabase exitosa, pero no devolvió datos.");
                                }
                                // Extract the first (and only) item from the list for the final response.
                                return Map.<String, Object>of(
                                        "auth", response,
                                        "db", dbResponseList.get(0));
                            });
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    String errorMessage = e.getMessage() != null ? e.getMessage()
                            : "A truly unknown error occurred, check server logs for stack trace.";
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
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    public Mono<Map<String, Object>> getUser(String accessToken) {
        return webClient.get()
                .uri("/auth/v1/user")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
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
                                .map(userPlanifika -> {
                                    Map<String, Object> response = new HashMap<>();
                                    response.put("userId", userPlanifika.getIdUser());
                                    response.put("name", userPlanifika.getName());
                                    response.put("email", supabaseUser.get("email"));
                                    response.put("photoUrl", userPlanifika.getPhotoUrl());
                                    response.put("userType", userPlanifika.getIdUserType());
                                    response.put("idusertype", userPlanifika.getIdUserType());
                                    response.put("iduserstatus", userPlanifika.getIdUserStatus());
                                    response.put("idorganization", userPlanifika.getIdOrganization());
                                    response.put("supabaseUserId", userPlanifika.getSupabaseUserId());
                                    response.put("created", false);
                                    return response;
                                })
                                .map(Mono::just)
                                .orElseGet(() -> {
                                    // Auto-provision del usuario si no existe
                                    try {
                                        UserPlanifika newUser = new UserPlanifika();
                                        newUser.setSupabaseUserId(supabaseUserId);
                                        // Intentar extraer metadata de Supabase
                                        String email = (String) supabaseUser.get("email");
                                        String name = null;
                                        String photoUrl = null;
                                        Object metaObj = supabaseUser.get("user_metadata");
                                        if (metaObj instanceof Map<?, ?> metaMap) {
                                            Object fullName = metaMap.get("full_name");
                                            Object rawName = metaMap.get("name");
                                            name = fullName != null ? fullName.toString()
                                                    : (rawName != null ? rawName.toString() : null);
                                            Object photo = metaMap.get("photourl");
                                            if (photo == null)
                                                photo = metaMap.get("avatar_url");
                                            photoUrl = photo != null ? photo.toString() : null;
                                        }
                                        if (name == null && email != null) {
                                            name = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
                                        }
                                        newUser.setName(name);
                                        newUser.setPhotoUrl(photoUrl);
                                        newUser.setIdUserStatus(UserStatusEnum.ACTIVE.getId());
                                        // Derivar tipo de usuario por rol del JWT si existe (admin => 2, default => 1)
                                        Integer derivedType = 1;
                                        Object roleClaim = supabaseUser.get("role");
                                        if (roleClaim != null && roleClaim.toString().equalsIgnoreCase("admin")) {
                                            derivedType = 2; // Ajusta según tu catálogo real
                                        }
                                        Object userRoleClaim = supabaseUser.get("user_role");
                                        if (userRoleClaim != null
                                                && userRoleClaim.toString().equalsIgnoreCase("admin")) {
                                            derivedType = 2;
                                        }
                                        newUser.setIdUserType(derivedType);
                                        newUser.setIdOrganization(null);

                                        UserPlanifika saved = userRepository.save(newUser);
                                        Map<String, Object> response = new HashMap<>();
                                        response.put("userId", saved.getIdUser());
                                        response.put("name", saved.getName());
                                        response.put("email", email);
                                        response.put("photoUrl", saved.getPhotoUrl());
                                        response.put("userType", saved.getIdUserType());
                                        response.put("idusertype", saved.getIdUserType());
                                        response.put("iduserstatus", saved.getIdUserStatus());
                                        response.put("idorganization", saved.getIdOrganization());
                                        response.put("supabaseUserId", saved.getSupabaseUserId());
                                        response.put("created", true);
                                        return Mono.just(response);
                                    } catch (Exception ex) {
                                        return Mono.error(new RuntimeException(
                                                "Error al auto-provisionar usuario local: " + ex.getMessage()));
                                    }
                                });

                    } catch (IllegalArgumentException e) {
                        return Mono
                                .error(new RuntimeException("Error al parsear el ID de Supabase: " + e.getMessage()));
                    } catch (Exception e) {
                        return Mono.error(
                                new RuntimeException("Error al obtener información del usuario: " + e.getMessage()));
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Error en getUserWithDatabaseInfo: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.error(e);
                });
    }

    public Mono<Map<String, Object>> updateProfile(String accessToken, String name, String password, String photourl) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasPassword = password != null && !password.isBlank();
        boolean hasPhoto = photourl != null && !photourl.isBlank();
        if (!hasName && !hasPassword && !hasPhoto) {
            return Mono.error(
                    new IllegalArgumentException("Debe proporcionar 'name', 'password' o 'photourl' para actualizar"));
        }

        return getUser(accessToken)
                .flatMap((Map<String, Object> supabaseUser) -> {
                    String supabaseUserIdStr = (String) supabaseUser.get("id");
                    if (supabaseUserIdStr == null) {
                        return Mono.error(new RuntimeException("No se encontró el ID del usuario en Supabase"));
                    }

                    UUID supabaseUserId;
                    try {
                        supabaseUserId = UUID.fromString(supabaseUserIdStr);
                    } catch (IllegalArgumentException e) {
                        return Mono
                                .error(new RuntimeException("ID de usuario Supabase inválido: " + supabaseUserIdStr));
                    }

                    Mono<Map<String, Object>> authUpdateMono;
                    if (hasName || hasPassword || hasPhoto) {
                        Map<String, Object> payload = new HashMap<>();
                        if (hasPassword) {
                            payload.put("password", password);
                        }
                        if (hasName || hasPhoto) {
                            Map<String, Object> data = new HashMap<>();
                            if (hasName) {
                                data.put("name", name);
                                data.put("full_name", name);
                            }
                            if (hasPhoto) {
                                data.put("photourl", photourl);
                            }
                            payload.put("data", data);
                        }

                        authUpdateMono = webClient.put()
                                .uri("/auth/v1/user")
                                .headers(h -> h.setBearerAuth(accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(payload)
                                .retrieve()
                                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                        clientResponse -> clientResponse.bodyToMono(String.class)
                                                .defaultIfEmpty("[No response body]")
                                                .flatMap(errorBody -> Mono
                                                        .error(new RuntimeException("Supabase Auth Update Error ("
                                                                + clientResponse.statusCode() + "): " + errorBody))))
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                                });
                    } else {
                        authUpdateMono = Mono.just(Map.of("skipped", true));
                    }

                    Mono<Map<String, Object>> dbUpdateMono;
                    if (hasName || hasPhoto) {
                        dbUpdateMono = Mono.fromCallable(() -> {
                            return userRepository.findBySupabaseUserId(supabaseUserId)
                                    .map(userPlanifika -> {
                                        if (hasName)
                                            userPlanifika.setName(name);
                                        if (hasPhoto)
                                            userPlanifika.setPhotoUrl(photourl);
                                        UserPlanifika saved = userRepository.save(userPlanifika);
                                        Map<String, Object> result = new HashMap<>();
                                        result.put("iduser", saved.getIdUser());
                                        result.put("name", saved.getName());
                                        result.put("photourl", saved.getPhotoUrl());
                                        return result;
                                    })
                                    .orElseThrow(() -> new RuntimeException(
                                            "Usuario no encontrado en la base de datos de la aplicación"));
                        })
                                .onErrorResume(e -> Mono.error(new RuntimeException(
                                        "Error al actualizar datos en la base de datos: " + e.getMessage())));
                    } else {
                        dbUpdateMono = Mono.just(Map.of("skipped", true));
                    }

                    return Mono.zip(authUpdateMono, dbUpdateMono)
                            .map(tuple -> Map.<String, Object>of(
                                    "auth", tuple.getT1(),
                                    "db", tuple.getT2(),
                                    "supabaseUserId", supabaseUserId));
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    String errorMessage = e.getMessage() != null ? e.getMessage()
                            : "Error desconocido al actualizar el perfil";
                    return Mono.error(new RuntimeException(errorMessage));
                });
    }
}
