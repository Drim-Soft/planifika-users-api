package com.planifikausersapi.usersapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.planifikausersapi.usersapi.repository.siu",
    entityManagerFactoryRef = "siuEntityManagerFactory",
    transactionManagerRef = "siuTransactionManager"
)
public class SIUJpaConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.siu")
    public DataSourceProperties siuDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Lazy
    public DataSource siuDataSource() {
        DataSourceProperties properties = siuDataSourceProperties();
        
        HikariConfig config = new HikariConfig();
        
        // Usar Supabase Pooler (puerto 6543)
        String url = properties.getUrl();
        if (url != null && url.contains(":5432/")) {
            url = url.replace(":5432/", ":6543/");
        }
        
        config.setJdbcUrl(url);
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        
        // Configuración mínima
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);
        config.setConnectionTimeout(20000);
        config.setInitializationFailTimeout(-1);
        config.setLeakDetectionThreshold(60000);
        config.setPoolName("SiuPool");
    // Ajustes PgJDBC para compatibilidad con PgBouncer (Supabase Pooler)
    config.addDataSourceProperty("prepareThreshold", "0");
    config.addDataSourceProperty("preferQueryMode", "simple");
    config.addDataSourceProperty("autosave", "always");
        
        return new HikariDataSource(config);
    }

    @Bean
    @Lazy
    public LocalContainerEntityManagerFactoryBean siuEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("siuDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.physical_naming_strategy", 
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        return builder
            .dataSource(dataSource)
            .packages("com.planifikausersapi.usersapi.model")
            .persistenceUnit("siu")
            .properties(properties)
            .build();
    }

    @Bean
    @Lazy
    public PlatformTransactionManager siuTransactionManager(
            @Qualifier("siuEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
