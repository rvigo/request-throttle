package com.example.demo.throttling.services.impl

import com.example.demo.throttling.services.Throttling
import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Bucket
import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.entities.ClientControl
import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.example.demo.throttling.repositories.ClientRepository
import com.example.demo.throttling.utils.TimeConversionUtil.Companion.getLocalDateTimeFromLong
import com.example.demo.throttling.utils.TimeConversionUtil.Companion.getTimeInMillis
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timerTask


class SlidingWindowCounterThrottle(
	private val clientRepository: ClientRepository,
	private val redisAtomicLongFactory: RedisAtomicLongFactory,
	private val defaultRateValue: Long,
	private val bucketService: BucketService
) : Throttling {
	private val log = LoggerFactory.getLogger(this::class.java)

	private val clientCallsCountMap = ConcurrentHashMap<String, ClientControl>()
	private val bucket: Bucket

	init {
		loadMappedClients()
		bucket = bucketService.getBucket()
		Timer(true).schedule(timerTask {
			bucketService.clearBucket(bucket)
		}, 0, bucket.renovationPeriod)
	}

	override fun throttle(clientIdentifier: String) {
		synchronized(this) {
			val client = findClient(clientIdentifier)
			clientCallsCountMap.putIfAbsent(client.name, ClientControl(redisAtomicLongFactory.of(client.name), client))
			when (isThrottled(client)) {
				true -> handleThrottledClient(client)
				false -> clientRepository.save(incrementCounter(client))
					.also { log.info("$it - callsCount: ${clientCallsCountMap[it.name]?.calls?.get()}") }
			}
		}
	}

	override fun isThrottled(client: Client) =
		synchronized(this) {
			(System.currentTimeMillis() - client.lastCallTimestamp) <= (bucket.renovationPeriod + bucketService.getLastBucketRenovation())
				&& clientCallsCountMap[client.name]!!.calls.get() >= client.rate
		}

	private fun incrementCounter(client: Client): Client =
		synchronized(this) {
			clientCallsCountMap[client.name]?.calls?.incrementAndGet().also {
				client.lastCallTimestamp = getTimeInMillis()
			}
			return client
		}

	private fun handleThrottledClient(client: Client) {
		synchronized(this) {
			if (bucketService.getLastBucketRenovation() > client.lastCallTimestamp)
				resetClient(client)
			else
				throw TooManyRequestsException(
					"too many requests for ${client.name}",
					HttpStatus.TOO_MANY_REQUESTS

				).also {
					incrementCounter(client)
					log.error("too many requests for $client")
				}
		}
	}

	private fun resetClient(client: Client) {
		synchronized(this) {
			client.apply {
				clientCallsCountMap[client.name]?.calls?.set(0)
				lastCallTimestamp = getTimeInMillis()
			}.also {
				clientCallsCountMap[client.name]?.client = it
				clientRepository.save(it)
			}
		}
	}

	private fun findClient(clientIdentifier: String): Client =
		synchronized(this) {
			if (clientCallsCountMap.containsKey(clientIdentifier)
				&& clientCallsCountMap[clientIdentifier]!!.isMapped()
			) clientCallsCountMap[clientIdentifier]?.client!!
				.also {
					log.info("got client $it from mapped set")
				} else clientRepository.findByIdOrNull(clientIdentifier) ?: Client(
				name = clientIdentifier,
				rate = defaultRateValue,
				lastCallTimestamp = 0L
			).also {
				clientRepository.save(it)
				log.info("new Client: $it")
			}
		}

	private fun loadMappedClients() {
		val mappedClients = clientRepository.findAllClientsByIsMappedTrue()
		clientCallsCountMap.putAll(mappedClients.associate {
			it.name to ClientControl(
				redisAtomicLongFactory.of(it.name),
				it
			)
		}).also { log.debug("loaded mapped clients: $clientCallsCountMap") }
	}
}
