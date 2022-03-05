package com.example.demo.throttling.entities

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("client")
data class Client(@Id val name: String, val rate: Int)
