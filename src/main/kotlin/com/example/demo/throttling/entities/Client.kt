package com.example.demo.throttling.entities

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.time.*
import java.util.concurrent.atomic.AtomicLong

@RedisHash("client")
open class Client(
	@Id
	var name: String,
	var rate: Int,
	var lastCallTimestamp: Long = System.currentTimeMillis()

) : Serializable {
	override fun toString(): String =
		"Client(name=$name, rate=$rate), lastCallTimestamp=${
			LocalDateTime.ofEpochSecond(
				lastCallTimestamp / 1000,
				((lastCallTimestamp % 1000 * 1000000).toInt()), ZoneOffset.UTC
			)
		}"
}
