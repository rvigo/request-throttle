package com.example.demo.throttling.services

import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Bucket
import com.example.demo.throttling.repositories.BucketRepository
import com.example.demo.throttling.services.impl.BucketService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.support.atomic.RedisAtomicLong
import org.springframework.data.repository.findByIdOrNull

class BucketServiceTest {
	private lateinit var bucketService: BucketService

	@MockK
	private lateinit var bucketRepository: BucketRepository

	@MockK
	private lateinit var redisAtomicLongFactory: RedisAtomicLongFactory

	@MockK
	private lateinit var lastBucketRenovation: RedisAtomicLong

	private val bucketId = "bucketId"
	private val bucketRenovationPeriod = 5L
	private lateinit var bucket: Bucket


	@BeforeEach
	fun setUp() {
		MockKAnnotations.init(this)
		every { redisAtomicLongFactory.of(any()) } returns lastBucketRenovation
		bucketService = BucketService(bucketRepository, bucketId, bucketRenovationPeriod, redisAtomicLongFactory)
		bucket = Bucket(bucketId, bucketRenovationPeriod)

	}

	@Test
	fun `should return an existent bucket`() {
		every { bucketRepository.findByIdOrNull(any()) } returns bucket

		val response = bucketService.getBucket()

		assertNotNull(response)
		assertEquals(bucketId, bucket.id)
		verify(exactly = 1) { bucketRepository.findByIdOrNull(any()) }
		verify(exactly = 0) { bucketRepository.save(any()) }
	}

	@Test
	fun `should create a new bucket`() {
		every { bucketRepository.findByIdOrNull(any()) } returns null
		every { bucketRepository.save(any()) } returns bucket

		val response = bucketService.getBucket()

		assertNotNull(response)
		assertEquals(bucketId, bucket.id)
		verify(exactly = 1) { bucketRepository.findByIdOrNull(any()) }
		verify(exactly = 1) { bucketRepository.save(any()) }
	}

	@Test
	fun `should clears the current bucket`() {
		every { lastBucketRenovation.set(any()) } just Runs
		every { lastBucketRenovation.get() } returns (System.currentTimeMillis() - bucketRenovationPeriod * 100)

		bucketService.clearBucket(bucket)

		verify(exactly = 1) { lastBucketRenovation.set(any()) }
	}

	@Test
	fun `should get the last bucket renovation timestamp`() {
		every { lastBucketRenovation.get() } returns System.currentTimeMillis()

		val result = bucketService.getLastBucketRenovation()

		assertNotNull(result)
	}
}
