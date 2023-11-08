package com.example.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBConfig {

    @Value("${mongodb.host}")
    private String host;

    @Value("${mongodb.database}")
    private String database;

    @Bean(destroyMethod = "close")
    public MongoClient mongoClient() {
        return MongoClients.create(host);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(database);
    }
}
