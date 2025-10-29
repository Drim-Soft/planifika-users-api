package com.planifikausersapi.usersapi.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public DataSource siuDataSource() {
        return siuDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean siuEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("siuDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.physical_naming_strategy", 
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        
        return builder
            .dataSource(dataSource)
            .packages("com.planifikausersapi.usersapi.model")
            .persistenceUnit("siu")
            .properties(properties)
            .build();
    }

    @Bean
    public PlatformTransactionManager siuTransactionManager(
            @Qualifier("siuEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
