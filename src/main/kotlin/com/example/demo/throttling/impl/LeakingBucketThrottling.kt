package com.example.demo.throttling.impl

import com.example.demo.throttling.ThrottlingType
import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.example.demo.throttling.repositories.ClientRepository
import com.example.demo.throttling.utils.TimeConversionUtil.Companion.getLocalDateTimeFromLong
import com.example.demo.throttling.utils.TimeConversionUtil.Companion.getTimeInMillis
import org.slf4j.LoggerFactory
import org.springframework.data.redis.support.atomic.RedisAtomicLong
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.timerTask


class LeakingBucketThrottling(
	private val clientRepository: ClientRepository,
	private val redisAtomicLongFactory: RedisAtomicLongFactory,
	private val bucketRenovationPeriod: Long,
	private val defaultRateValue: Int
) : ThrottlingType {
	private val log = LoggerFactory.getLogger(this::class.java)

	private val lastBucketRenovation = AtomicLong(getTimeInMillis())
	private val clientCallsCountMap = ConcurrentHashMap<String, RedisAtomicLong>()

	init {
		Timer(true).schedule(timerTask {
			resetBucket()
		}, 0, bucketRenovationPeriod)
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
					lastCallTimestamp = 0
				).also { addClient(it) }

			when (isThrottled(client)) {
				true -> handleThrottledClient(client)
				false -> {
					clientCallsCountMap[client.name]?.incrementAndGet()
					clientRepository.save(client.apply {
						lastCallTimestamp = getTimeInMillis()
					}).also { log.info("$it - callsCount: ${clientCallsCountMap[it.name]?.get()}") }
				}
			}
		}
	}

	private fun resetBucket() {
		lastBucketRenovation.set(getTimeInMillis())
	}

	override fun isThrottled(client: Client) =
		((System.currentTimeMillis() - client.lastCallTimestamp) <= (bucketRenovationPeriod + lastBucketRenovation.get())
			&& clientCallsCountMap[client.name]!!.get() >= client.rate)

	private fun handleThrottledClient(client: Client) {
		if (lastBucketRenovation.get() > client.lastCallTimestamp)
			resetClient(client)
		else
			throw TooManyRequestsException(
				"too many requests for ${client.name}",
				HttpStatus.TOO_MANY_REQUESTS
			).also { log.error("too many requests for ${client.name}") }
	}

	private fun resetClient(client: Client) {
		client.apply {
			clientCallsCountMap[client.name]?.set(0)
			lastCallTimestamp = getTimeInMillis()
		}.also { clientRepository.save(it) }
	}

	override fun toString(): String =
		"LeakingBucketThrottling(lastUpdate=${
			getLocalDateTimeFromLong(lastBucketRenovation.get())
		}, renovationTime=$bucketRenovationPeriod, defaultRateValue=$defaultRateValue)"
}
