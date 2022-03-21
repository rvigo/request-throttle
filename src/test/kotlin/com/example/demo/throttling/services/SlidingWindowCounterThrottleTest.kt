package com.example.demo.throttling.services

import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Bucket
import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.example.demo.throttling.repositories.BucketRepository
import com.example.demo.throttling.repositories.ClientRepository
import com.example.demo.throttling.services.impl.BucketService
import com.example.demo.throttling.services.impl.SlidingWindowCounterThrottle
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.support.atomic.RedisAtomicLong
import org.springframework.data.repository.findByIdOrNull

class SlidingWindowCounterThrottleTest {
	private lateinit var slidingWindowCounterThrottle: SlidingWindowCounterThrottle

	@MockK
	private lateinit var clientRepository: ClientRepository

	@MockK
	private lateinit var bucketRepository: BucketRepository

	@MockK
	private lateinit var redisAtomicLongFactory: RedisAtomicLongFactory

	private lateinit var bucketService: BucketService

	@MockK
	private lateinit var calls: RedisAtomicLong

	@MockK
	private lateinit var lastBucketRenovation: RedisAtomicLong

	private val clientId = "clientId"
	private val bucketId = "bucketId"
	private val defaultRateValue = 5L
	private val renovationValue = 5L
	private lateinit var bucket: Bucket
	private lateinit var client: Client

	//TODO Mockar corretamente o BucketService
	@BeforeEach
	fun setUp() {
		MockKAnnotations.init(this)

		client = Client(name = clientId, rate = defaultRateValue)
		bucket = Bucket(id = bucketId, renovationPeriod = renovationValue)

		every { redisAtomicLongFactory.of(clientId) } returns calls
		every { redisAtomicLongFactory.of(bucketId) } returns lastBucketRenovation

		every { lastBucketRenovation.set(any()) } just Runs
		every { lastBucketRenovation.get() } returns System.currentTimeMillis() - 999999

		every { calls.get() } returns 999
		every { calls.set(any()) } just Runs
		every { calls.incrementAndGet() } returns 1000

		every { bucketRepository.findByIdOrNull(any()) } returns bucket

		every { clientRepository.findAllClientsByIsMappedTrue() } returns listOf(client)

		bucketService = BucketService(bucketRepository, bucketId, renovationValue, redisAtomicLongFactory)
		slidingWindowCounterThrottle =
			SlidingWindowCounterThrottle(clientRepository, redisAtomicLongFactory, defaultRateValue, bucketService)
	}

	@Test
	fun `should throttle recurrent calls`() {
		every { clientRepository.findByIdOrNull(any()) } returns client
		every { clientRepository.save(any()) } returns client

		assertThrows<TooManyRequestsException> { slidingWindowCounterThrottle.throttle(clientId) }
	}

	@Test
	fun `should reset a client calls count`() {
		every { lastBucketRenovation.get() } returns System.currentTimeMillis()
		every { clientRepository.findByIdOrNull(any()) } returns client
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 1) {
			calls.set(0)
			clientRepository.save(client)
		}
	}

	@Test
	fun `should return a call`() {
		every { calls.get() } returns 0
		every { lastBucketRenovation.get() } returns System.currentTimeMillis()
		every { clientRepository.findByIdOrNull(any()) } returns client
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 1) {
			calls.incrementAndGet()
			clientRepository.save(client)
		}
	}

	@Test
	fun `should return a call of a unexpected client`() {
		every { lastBucketRenovation.get() } returns System.currentTimeMillis()
		every { clientRepository.findByIdOrNull(any()) } returns null
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 2) { clientRepository.save(any()) }
	}

	@Test
	fun `should return a call of a mapped client`() {
		client.apply { isMapped = true }
		every { lastBucketRenovation.get() } returns System.currentTimeMillis()
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 1) { clientRepository.save(any()) }
	}
}
