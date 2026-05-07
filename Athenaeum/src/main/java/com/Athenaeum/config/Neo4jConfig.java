package com.Athenaeum.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    @Value("${neo4j.uri:bolt://localhost:7687}")
    private String neo4jUri;

    @Value("${neo4j.username:neo4j}")
    private String neo4jUsername;

    @Value("${neo4j.password:password}")
    private String neo4jPassword;

    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(neo4jUri, AuthTokens.basic(neo4jUsername, neo4jPassword));
    }
}