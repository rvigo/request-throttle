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
import org.junit.jupiter.api.Disabled
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
	private lateinit var redisAtomicLongFactory: RedisAtomicLongFactory

	@MockK
	private lateinit var bucketService: BucketService

	@MockK
	private lateinit var calls: RedisAtomicLong

	private val clientId = "clientId"
	private val bucketId = "bucketId"
	private val defaultRateValue = 5L
	private val renovationValue = 1000L
	private lateinit var bucket: Bucket
	private lateinit var client: Client

	@BeforeEach
	fun setUp() {
		MockKAnnotations.init(this)

		client = Client(name = clientId, rate = defaultRateValue)
		bucket = Bucket(id = bucketId, renovationPeriod = renovationValue)

		every { calls.get() } returns 1
		every { calls.set(any()) } just Runs
		every { calls.incrementAndGet() } returns 2

		every { redisAtomicLongFactory.of(any()) } returns calls

		every { bucketService.getBucket() } returns bucket
		every { bucketService.getLastBucketRenovationTime() } returns System.currentTimeMillis()
		every { bucketService.clearBucket() } just Runs

		every { clientRepository.findAllClientsByIsMappedTrue() } returns listOf(client)

		slidingWindowCounterThrottle =
			SlidingWindowCounterThrottle(clientRepository, redisAtomicLongFactory, defaultRateValue, bucketService)
	}

	@Test
	fun `should throttle recurrent calls`() {
		client.apply { lastCallTime = 500 }
		every { bucketService.getLastBucketRenovationTime() } returns 1
		every { calls.get() } returns client.rate + 1
		every { clientRepository.findByIdOrNull(any()) } returns client
		every { clientRepository.save(any()) } returns client

		assertThrows<TooManyRequestsException> { slidingWindowCounterThrottle.throttle(clientId) }
	}

	@Test
	fun `should reset a client calls count`() {
		client.apply { lastCallTime = 0 }
		every { calls.get() } returns 5
		every { calls.incrementAndGet() } returns 1

		every { bucketService.getLastBucketRenovationTime() } returns 1L
		every { clientRepository.findByIdOrNull(any()) } returns client
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 1) {
			calls.set(0)
			calls.set(1)
		}
		verify(exactly = 2) { clientRepository.save(client) }
	}

	@Test
	fun `should return a call`() {
		every { calls.get() } returns 0
		every { clientRepository.findByIdOrNull(any()) } returns client
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 1) {
			calls.incrementAndGet()
		}
		verify(exactly = 2) { clientRepository.save(client) }
	}

	@Test
	fun `should return a call of a unexpected client`() {
		every { clientRepository.findByIdOrNull(any()) } returns null
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 3) { clientRepository.save(any()) }
	}

	@Test
	fun `should return a call of a mapped client`() {
		every { calls.get() } returns 0
		client.apply { isMapped = true }
		every { clientRepository.save(any()) } returns client

		assertDoesNotThrow { slidingWindowCounterThrottle.throttle(clientId) }

		verify(exactly = 2) { clientRepository.save(any()) }
	}

	@Test
	fun `should increment the call counter`() {
		var count = 0L
		every { calls.get() } answers {
			count = count.inc()
			count
		}
		every { clientRepository.findByIdOrNull(any()) } returns client
		every { clientRepository.save(any()) } returns client

		slidingWindowCounterThrottle.throttle(clientId)

		assertEquals(2, count)
		verify(exactly = 2) { calls.get() }
	}
}
