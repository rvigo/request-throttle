package com.example.demo.throttling.entities

import org.springframework.data.redis.support.atomic.RedisAtomicLong

data class ClientControl(val calls: RedisAtomicLong, var client: Client) {
	fun isMapped(): Boolean = client.isMapped
}
