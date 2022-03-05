package com.example.demo.throttling.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration

@Configuration
class RedisConfiguration {
	@Bean
	fun redisStandaloneConfiguration(
		@Value("\${spring.redis.host}") host: String,
		@Value("\${spring.redis.port}") port: Int
	) =
		RedisStandaloneConfiguration(host, port)
}
