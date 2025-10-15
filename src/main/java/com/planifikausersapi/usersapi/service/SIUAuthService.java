package com.planifikausersapi.usersapi.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class SIUAuthService {
  private final WebClient webClient;
  private final String supabaseUrl;
  private final String anonKey;

  public SIUAuthService(@Value("${supabase.url.siu}") String supabaseUrl,
      @Value("${supabase.anon.key.siu}") String anonKey,
      WebClient.Builder webClientBuilder) {
    this.supabaseUrl = supabaseUrl;
    this.anonKey = anonKey;
    this.webClient = webClientBuilder
        .baseUrl(supabaseUrl)
        .defaultHeader("apiKey", anonKey)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
        .build();
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
