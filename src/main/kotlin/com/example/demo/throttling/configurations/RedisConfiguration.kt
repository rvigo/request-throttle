package com.example.demo.throttling.configurations

import com.example.demo.throttling.entities.Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class RedisConfiguration {

	@Bean
	fun redisConnectionFactory(
		@Value("\${spring.redis.host}") host: String,
		@Value("\${spring.redis.port}") port: Int
	): JedisConnectionFactory {
		val redisStandaloneConfiguration = RedisStandaloneConfiguration(host, port)
		return JedisConnectionFactory(redisStandaloneConfiguration)
	}

	@Bean
	fun redisTemplate(
		@Value("\${spring.redis.host}") host: String,
		@Value("\${spring.redis.port}") port: Int,
		jedisConnectionFactory: JedisConnectionFactory
	): RedisTemplate<String, Client> {
		val redisTemplate = RedisTemplate<String, Client>()
		redisTemplate.setConnectionFactory(jedisConnectionFactory)
		redisTemplate.afterPropertiesSet()
		return redisTemplate
	}
}
