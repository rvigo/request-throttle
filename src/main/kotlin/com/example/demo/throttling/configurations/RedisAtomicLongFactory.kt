package com.example.demo.throttling.configurations

import com.example.demo.throttling.entities.Client
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.support.atomic.RedisAtomicLong
import org.springframework.stereotype.Component

@Component
class RedisAtomicLongFactory(private val jedisConnectionFactory: JedisConnectionFactory) {
	fun of(client: Client) = RedisAtomicLong(client.name, jedisConnectionFactory, 0)
}
