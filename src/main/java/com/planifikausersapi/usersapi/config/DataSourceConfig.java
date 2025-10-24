package com.planifikausersapi.usersapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

  // Para la base de datos principal
  @Primary
  @Bean(name = "dataSource")
  public DataSource dataSource(
      @Value("${spring.datasource.url}") String dbUrl,
      @Value("${spring.datasource.username}") String dbUsername,
      @Value("${spring.datasource.password}") String dbPassword,
      @Value("${spring.datasource.driver-class-name}") String dbDriverClassName) {
    return DataSourceBuilder.create()
        .url(dbUrl)
        .username(dbUsername)
        .password(dbPassword)
        .driverClassName(dbDriverClassName)
        .build();
  }

  // Para la segunda base de datos (SIU)
  @Bean(name = "dataSourceSIU")
  public DataSource dataSourceSIU(
      @Value("${spring.datasource.siu.url}") String dbUrlSIU,
      @Value("${spring.datasource.siu.username}") String dbUsernameSIU,
      @Value("${spring.datasource.siu.password}") String dbPasswordSIU,
      @Value("${spring.datasource.siu.driver-class-name}") String dbDriverClassNameSIU) {
    return DataSourceBuilder.create()
        .url(dbUrlSIU)
        .username(dbUsernameSIU)
        .password(dbPasswordSIU)
        .driverClassName(dbDriverClassNameSIU)
        .build();
  }
}