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
    basePackages = "com.planifikausersapi.usersapi.repository.drimsoft",
    entityManagerFactoryRef = "drimsoftEntityManagerFactory",
    transactionManagerRef = "drimsoftTransactionManager"
)
public class DrimsoftDataSourceConfig {

    @Bean
    @ConfigurationProperties("drimsoft.datasource")
    public DataSourceProperties drimsoftDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Lazy
    public DataSource drimsoftDataSource() {
        DataSourceProperties properties = drimsoftDataSourceProperties();
        
        HikariConfig config = new HikariConfig();
        
        // Usar Supabase Pooler (puerto 6543) en lugar de conexión directa (5432)
        String url = properties.getUrl();
        if (url != null && url.contains(":5432/")) {
            url = url.replace(":5432/", ":6543/");
        }
        
        config.setJdbcUrl(url);
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        
        // Configuración mínima para Supabase
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);
        config.setConnectionTimeout(20000);
        config.setInitializationFailTimeout(-1);
        config.setLeakDetectionThreshold(60000);
        config.setPoolName("DrimsoftPool");
    // Ajustes PgJDBC para compatibilidad con PgBouncer (Supabase Pooler)
    config.addDataSourceProperty("prepareThreshold", "0");
    config.addDataSourceProperty("preferQueryMode", "simple");
    config.addDataSourceProperty("autosave", "always");
        
        return new HikariDataSource(config);
    }

    @Bean
    @Lazy
    public LocalContainerEntityManagerFactoryBean drimsoftEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("drimsoftDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.physical_naming_strategy", 
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        return builder
            .dataSource(dataSource)
            .packages("com.planifikausersapi.usersapi.model")
            .persistenceUnit("drimsoft")
            .properties(properties)
            .build();
    }

    @Bean
    @Lazy
    public PlatformTransactionManager drimsoftTransactionManager(
            @Qualifier("drimsoftEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
