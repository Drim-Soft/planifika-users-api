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
    public DataSource drimsoftDataSource() {
        return drimsoftDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean drimsoftEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("drimsoftDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.physical_naming_strategy", 
            "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        
        return builder
            .dataSource(dataSource)
            .packages("com.planifikausersapi.usersapi.model")
            .persistenceUnit("drimsoft")
            .properties(properties)
            .build();
    }

    @Bean
    public PlatformTransactionManager drimsoftTransactionManager(
            @Qualifier("drimsoftEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
