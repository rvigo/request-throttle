package com.example.demo.throttling.configurations

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.support.atomic.RedisAtomicLong
import org.springframework.stereotype.Component

@Component
class RedisAtomicLongFactory(private val jedisConnectionFactory: JedisConnectionFactory) {
	fun of(id: String) = RedisAtomicLong(id, jedisConnectionFactory, 0)
}
