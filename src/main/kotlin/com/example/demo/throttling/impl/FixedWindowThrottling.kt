package com.example.demo.throttling.impl

import com.example.demo.throttling.ThrottlingType
import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.example.demo.throttling.repositories.ClientRepository
import com.example.demo.throttling.utils.TimeConversionUtil.Companion.getTimeInMillis
import org.slf4j.LoggerFactory
import org.springframework.data.redis.support.atomic.RedisAtomicLong
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timerTask

class FixedWindowThrottling(
	private val clientRepository: ClientRepository,
	private val redisAtomicLongFactory: RedisAtomicLongFactory,
	period: Long,
	private val defaultRateValue: Int
) : ThrottlingType {
	private val log = LoggerFactory.getLogger(this::class.java)

	private val clientCallsCountMap = ConcurrentHashMap<String, RedisAtomicLong>()

	init {
		Timer(true).schedule(timerTask {
			clientCallsCountMap.forEach { (_, v) -> v.set(0) }
		}, 0, period)
	}

	//TODO remover
	fun addClient(client: Client) {
		clientCallsCountMap.putIfAbsent(client.name, redisAtomicLongFactory.of(client))
		clientRepository.save(client)
	}

	override fun throttle(clientIdentifier: String) {
		synchronized(this) {
			val client = clientRepository.findByIdOrNull(clientIdentifier)
				?: Client(
					name = clientIdentifier,
					rate = defaultRateValue,
				).also { addClient(it) }

			when (isThrottled(client)) {
				true -> throw TooManyRequestsException(
					"too many requests for $clientIdentifier",
					HttpStatus.TOO_MANY_REQUESTS
				)
					.also { log.error("too many requests for $clientIdentifier") }
				false -> {
					clientCallsCountMap[clientIdentifier]?.incrementAndGet()
					clientRepository.save(client)
						.also { log.info("$it - callsCount: ${clientCallsCountMap[it.name]?.get()}") }
				}
			}
		}
	}

	override fun isThrottled(client: Client) = clientCallsCountMap[client.name]!!.get() >= client.rate
}
