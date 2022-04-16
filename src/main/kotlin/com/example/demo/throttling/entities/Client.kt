package com.example.demo.throttling.entities

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset

@RedisHash("client")
open class Client(
	@Id
	var name: String,
	var rate: Long,
	var lastCallTime: Long = System.currentTimeMillis(),
	@Indexed
	var isMapped: Boolean = false

) : Serializable {
	override fun toString(): String =
		"Client(name=$name, rate=$rate), lastCallTimestamp=${
			LocalDateTime.ofEpochSecond(
				lastCallTime / 1000,
				((lastCallTime % 1000 * 1000000).toInt()), ZoneOffset.UTC
			)
		}"
}
