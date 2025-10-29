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
  private final String serviceKey;

  public SIUAuthService(@Value("${supabase.url.siu}") String supabaseUrl,
      @Value("${supabase.anon.key.siu}") String anonKey,
      @Value("${supabase.service.key}") String serviceKey,
      WebClient.Builder webClientBuilder,
      UserService userService,
      SIUUserService siuUserService) {
    this.userService = userService;
    this.siuUserService = siuUserService;
    this.serviceKey = serviceKey;
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
          UUID supabaseUserId = UUID.fromString((String) ((Map<String, Object>) authResponse.get("user")).get("id"));

          try {
            UserPlanifika user = userService.findBySupabaseId(supabaseUserId);
            
            // Verificar si el usuario tiene datos incorrectos y corregirlos
            boolean needsUpdate = false;
            if (user.getIdUser() == null || user.getIdUser() == 0) {
              System.out.println("⚠️ ADVERTENCIA: Usuario tiene ID inválido: " + user.getIdUser());
              needsUpdate = true;
            }
            if (user.getIdUserType() == null || user.getIdUserType() != 3) {
              System.out.println("⚠️ ADVERTENCIA: Usuario no tiene idUserType=3. Actual: " + user.getIdUserType());
              user.setIdUserType(3); // Forzar tipo estudiante
              needsUpdate = true;
            }
            if (user.getIdUserStatus() == null || user.getIdUserStatus() == 0) {
              System.out.println("⚠️ ADVERTENCIA: Usuario no tiene estado activo. Actual: " + user.getIdUserStatus());
              user.setIdUserStatus(1); // Forzar estado activo
              needsUpdate = true;
            }
            
            if (needsUpdate && user.getIdUser() != null && user.getIdUser() > 0) {
              System.out.println("🔄 Actualizando usuario con ID: " + user.getIdUser());
              user = userService.save(user);
            }
            
            System.out.println("Usuario encontrado - ID: " + user.getIdUser() + ", Name: " + user.getName() + ", UserType: " + user.getIdUserType() + ", Status: " + user.getIdUserStatus());
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            // Para estudiantes (idUserType=3), devolver el SERVICE_KEY en lugar del token de usuario
            if (user.getIdUserType() != null && user.getIdUserType() == 3) {
              result.put("access_token", serviceKey);
              System.out.println("✅ Estudiante detectado (idUserType=3), usando SERVICE_KEY para autenticación");
              System.out.println("SERVICE_KEY: " + serviceKey.substring(0, 50) + "...");
            } else {
              result.put("access_token", (String) authResponse.get("access_token"));
              System.out.println("⚠️ Usuario NO es estudiante (idUserType=" + user.getIdUserType() + "), usando token de usuario");
            }
            return Mono.just(result);
          } catch (EntityNotFoundException e) {
            // Si no existe en nuestra base de datos, lo crea
            System.out.println("⚠️ Usuario no encontrado, creando nuevo usuario: " + supabaseUserId);
            UserPlanifika newUser = createUserFromExternalSystem(supabaseUserId);
            System.out.println("✅ Usuario creado - ID: " + newUser.getIdUser() + ", Name: " + newUser.getName() + ", UserType: " + newUser.getIdUserType() + ", Status: " + newUser.getIdUserStatus());
            Map<String, Object> result = new HashMap<>();
            result.put("user", newUser);
            // Para estudiantes nuevos (idUserType=3), devolver el SERVICE_KEY
            if (newUser.getIdUserType() != null && newUser.getIdUserType() == 3) {
              result.put("access_token", serviceKey);
              System.out.println("✅ Nuevo estudiante creado (idUserType=3), usando SERVICE_KEY para autenticación");
              System.out.println("SERVICE_KEY: " + serviceKey.substring(0, 50) + "...");
            } else {
              result.put("access_token", (String) authResponse.get("access_token"));
              System.out.println("⚠️ Nuevo usuario NO es estudiante (idUserType=" + newUser.getIdUserType() + "), usando token de usuario");
            }
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
    newUser.setIdUserType(3); // Estudiante
    newUser.setIdUserStatus(1); // Activo
    
    // El método save() retorna el usuario con el ID generado por la base de datos
    UserPlanifika savedUser = userService.save(newUser);
    System.out.println("Nuevo usuario creado con ID: " + savedUser.getIdUser());
    
    return savedUser;
  }

}
