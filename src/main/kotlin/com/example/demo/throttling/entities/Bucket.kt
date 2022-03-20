package com.example.demo.throttling.entities

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("bucket")
class Bucket(
	@Id
	var id: String? = null,
	var renovationPeriod: Long = 0L,
) {
	override fun toString(): String =
		"Bucket(id=$id, renovationPeriod=$renovationPeriod)"
}
