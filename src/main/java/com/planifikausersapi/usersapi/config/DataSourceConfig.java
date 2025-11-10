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
import org.springframework.context.annotation.Primary;
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
    basePackages = "com.planifikausersapi.usersapi.repository.planifika",
    entityManagerFactoryRef = "planifikaEntityManagerFactory",
    transactionManagerRef = "planifikaTransactionManager"
)
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties planifikaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource planifikaDataSource() {
        DataSourceProperties properties = planifikaDataSourceProperties();
        
        HikariConfig config = new HikariConfig();
        
        // Usar Supabase Pooler (puerto 6543)
        String url = properties.getUrl();
        if (url != null && url.contains(":5432/")) {
            url = url.replace(":5432/", ":6543/");
        }
        
        config.setJdbcUrl(url);
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        
        // Configuración para datasource principal
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);
        config.setConnectionTimeout(20000);
        config.setLeakDetectionThreshold(60000);
        config.setPoolName("PlanifikaPool");
    // Desactivar prepared statements en PgBouncer y evitar errores 42P05 / 25P02
    config.addDataSourceProperty("prepareThreshold", "0");
    config.addDataSourceProperty("preferQueryMode", "simple");
    // autosave=always evita abortar toda la transacción ante errores de statements intermedios
    config.addDataSourceProperty("autosave", "always");
        
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean planifikaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("planifikaDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.physical_naming_strategy", 
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        return builder
            .dataSource(dataSource)
            .packages("com.planifikausersapi.usersapi.model")
            .persistenceUnit("planifika")
            .properties(properties)
            .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager planifikaTransactionManager(
            @Qualifier("planifikaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}