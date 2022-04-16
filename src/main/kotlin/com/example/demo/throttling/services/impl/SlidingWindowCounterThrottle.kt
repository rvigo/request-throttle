package com.example.demo.throttling.services.impl

import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Bucket
import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.entities.ClientControl
import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.example.demo.throttling.repositories.ClientRepository
import com.example.demo.throttling.services.Throttling
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
	//TODO(não é relevante para a classe, mover)
	private val defaultRateValue: Long,
	private val bucketService: BucketService
) : Throttling {
	private val log = LoggerFactory.getLogger(this::class.java)

	private val clientCallsCountMap = ConcurrentHashMap<String, ClientControl>()

	//TODO(remover bucket)
	private val bucket: Bucket

	init {
		loadMappedClients()
		bucket = bucketService.getBucket()
		Timer(true).schedule(timerTask {
			bucketService.clearBucket()

		}, 0, bucket.renovationPeriod)
	}

	override fun throttle(clientIdentifier: String) {
		synchronized(this) {
			val client = findClient(clientIdentifier)
			when (client.isThrottled()) {
				true -> throw TooManyRequestsException(
					"too many requests for ${client.name}",
					HttpStatus.TOO_MANY_REQUESTS
				).also { log.error("too many requests for ${client.name}") }
				false -> {
					if (bucketService.getLastBucketRenovationTime() `is newer than` client.lastCallTime) {
						resetClientInfo(client.name)
							.also { log.info("resetting ${client.name} info due to outdated renovation period") }
					}
					updateClientInfo(client.name)
				}
			}
		}
	}

	override fun Client.isThrottled() =
		synchronized(this) {
			clientCallsCountMap[this.name]!!.calls.get() >= this.rate
				&& this.lastCallTime `is older than` (bucket.renovationPeriod
				+ bucketService.getLastBucketRenovationTime())
				&& this.lastCallTime `is newer than` bucketService.getLastBucketRenovationTime()
		}

	private fun updateClientInfo(clientName: String) {
		synchronized(this) {
			with(clientCallsCountMap[clientName]!!) {
				this.calls.set(this.calls.incrementAndGet())
				this.client.lastCallTime = getTimeInMillis()

				clientRepository.save(this.client)
					.also { log.info("$it - calls: ${clientCallsCountMap[clientName]!!.calls.get()}") }
			}
		}
	}

	private fun resetClientInfo(clientName: String) {
		synchronized(this) {
			clientCallsCountMap[clientName]?.let {
				it.calls.set(0)
				with(it.client) {
					lastCallTime = System.currentTimeMillis()
					clientRepository.save(this)
				}.also { client -> log.info("resetting ${client.name} info") }
			}
		}
	}

	private fun findClient(clientIdentifier: String): Client =
		synchronized(this) {
			return@findClient when (clientCallsCountMap.containsKey(clientIdentifier)
				&& clientCallsCountMap[clientIdentifier]!!.isMapped()
			) {
				true -> clientCallsCountMap[clientIdentifier]!!.client
					.also { log.info("got client $it from mapped set") }
				false -> {
					val client = clientRepository.findByIdOrNull(clientIdentifier)
						?: Client(
							name = clientIdentifier,
							rate = defaultRateValue,
							lastCallTime = 0L
						).also {
							log.info("creating new client")
							clientRepository.save(it)
						}
					when (clientCallsCountMap.containsKey(clientIdentifier)) {
						true -> {
							clientCallsCountMap[clientIdentifier]!!.client = client
							return@findClient client
						}
						false -> {
							clientCallsCountMap[clientIdentifier] =
								ClientControl(redisAtomicLongFactory.of(client.name), client)
							return@findClient client
						}
					}
				}
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
