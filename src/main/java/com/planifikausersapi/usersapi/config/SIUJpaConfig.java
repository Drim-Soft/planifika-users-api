package com.planifikausersapi.usersapi.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(basePackages = "com.planifikausersapi.usersapi.repository", includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.REGEX, pattern = ".*SIU.*"), entityManagerFactoryRef = "entityManagerFactorySIU", transactionManagerRef = "transactionManagerSIU")
public class SIUJpaConfig {

  @Bean(name = "entityManagerFactorySIU")
  public LocalContainerEntityManagerFactoryBean entityManagerFactorySIU(
      EntityManagerFactoryBuilder builder,
      @Qualifier("dataSourceSIU") DataSource dataSourceSIU) {
    return builder
        .dataSource(dataSourceSIU)
        .packages("com.planifikausersapi.usersapi.model")
        .persistenceUnit("siu")
        .build();
  }

  @Bean(name = "transactionManagerSIU")
  public PlatformTransactionManager transactionManagerSIU(
      @Qualifier("entityManagerFactorySIU") EntityManagerFactory entityManagerFactorySIU) {
    return new JpaTransactionManager(entityManagerFactorySIU);
  }
}
