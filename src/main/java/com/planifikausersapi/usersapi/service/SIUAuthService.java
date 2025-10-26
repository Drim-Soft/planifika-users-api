package com.planifikausersapi.usersapi.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.planifikausersapi.usersapi.model.UserPlanifika;
import com.planifikausersapi.usersapi.model.UserSIU;

import jakarta.persistence.EntityNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class SIUAuthService {
  private final WebClient webClient;
  private final UserService userService;
  private final SIUUserService siuUserService;

  public SIUAuthService(@Value("${supabase.url.siu}") String supabaseUrl,
      @Value("${supabase.anon.key.siu}") String anonKey,
      WebClient.Builder webClientBuilder,
      UserService userService,
      SIUUserService siuUserService) {
    this.userService = userService;
    this.siuUserService = siuUserService;
    this.webClient = webClientBuilder
        .baseUrl(supabaseUrl)
        .defaultHeader("apiKey", anonKey)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
        .build();
  }

  public Mono<Map<String, Object>> signIn(String email, String password) {
    return webClient.post()
        .uri(uriBuilder -> uriBuilder.path("/auth/v1/token")
            .queryParam("grant_type", "password")
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of("email", email, "password", password))
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
            clientResponse -> Mono.error(new RuntimeException("Error durante la petición de autenticación")))
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
        });
  }

  public Mono<Map<String, Object>> externalLogin(String email, String password) {
    return signIn(email, password)
        .flatMap(authResponse -> {
          String accessToken = (String) authResponse.get("access_token");
          UUID supabaseUserId = UUID.fromString((String) ((Map<String, Object>) authResponse.get("user")).get("id"));

          try {
            UserPlanifika user = userService.findBySupabaseId(supabaseUserId);
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("access_token", accessToken);
            return Mono.just(result);
          } catch (EntityNotFoundException e) {
            // Si no existe en nuestra base de datos, lo crea
            System.out.println("Usuario no encontrado, creando nuevo usuario: " + supabaseUserId);
            UserPlanifika newUser = createUserFromExternalSystem(supabaseUserId);
            Map<String, Object> result = new HashMap<>();
            result.put("user", newUser);
            result.put("access_token", accessToken);
            return Mono.just(result);
          } catch (Exception e) {
            // Para otros errores (como problemas de conexión), re-lanzamos la excepción
            System.out.println("ERROR inesperado: " + e.getMessage());
            return Mono.error(e);
          }
        });
  }

  // Crear un nuevo usuario a partir de la información obtenida del sistema
  // externo
  private UserPlanifika createUserFromExternalSystem(UUID supabaseUserId) {
    UserSIU userSIU = siuUserService.findBySupabaseId(supabaseUserId);

    UserPlanifika newUser = new UserPlanifika();
    newUser.setName(userSIU.getName());
    newUser.setPhotoUrl(userSIU.getPhotoUrl());
    newUser.setSupabaseUserId(supabaseUserId);

    return userService.save(newUser);
  }

}
