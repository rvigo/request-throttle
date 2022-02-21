package com.example.demo.throttling.configurations

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration

@Configuration
class MongoDbConfiguration : AbstractMongoClientConfiguration() {
    private val log = KotlinLogging.logger {}

    @Value("\${spring.data.mongodb.database}")
    private val database: String? = null

    @Value("\${spring.data.mongodb.host}")
    private val host: String? = null

    @Value("\${spring.data.mongodb.port}")
    private val port: String? = null

    override fun getDatabaseName(): String {
        return database ?: "ThrottleClients"
    }

    override fun mongoClient(): MongoClient {
        val connectionString = ConnectionString("mongodb://$host:$port/$database")
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        return MongoClients.create(mongoClientSettings).also {
            log.info { "connected to mongodb: $host:$port/$database" }
        }
    }
}