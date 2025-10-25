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
    basePackages = "com.planifikausersapi.usersapi.repository.planifika",
    entityManagerFactoryRef = "planifikaEntityManagerFactory",
    transactionManagerRef = "planifikaTransactionManager"
)
public class PlanifikaDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties planifikaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource planifikaDataSource() {
        return planifikaDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean planifikaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("planifikaDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.physical_naming_strategy", 
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        
        return builder
            .dataSource(dataSource)
            .packages("com.planifikausersapi.usersapi.dto", 
                     "com.planifikausersapi.usersapi.model")
            .persistenceUnit("planifika")
            .properties(properties)
            .build();
    }

    @Bean
    public PlatformTransactionManager planifikaTransactionManager(
            @Qualifier("planifikaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
