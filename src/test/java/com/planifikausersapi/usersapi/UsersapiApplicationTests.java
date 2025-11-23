package com.planifikausersapi.usersapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "supabase.url=http://localhost:54321",
        "supabase.anon.key=test-anon-key",
        "supabase.url.siu=http://localhost:54321",
        "supabase.anon.key.siu=test-anon-key-siu",
        "supabase.service.key=test-service-key",
        "supabase.jwt.secret=test-jwt-secret-key-for-testing-purposes-only",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "drimsoft.datasource.url=jdbc:h2:mem:testdb2",
        "drimsoft.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.siu.url=jdbc:h2:mem:testdb3",
        "spring.datasource.siu.driver-class-name=org.h2.Driver"
    }
)
class UsersapiApplicationTests {

	@Test
	void contextLoads() {
		// Este test solo verifica que el contexto de Spring se puede cargar
		// SecurityConfig está condicionado con @ConditionalOnWebApplication, por lo que no se carga con webEnvironment = NONE
	}

}
