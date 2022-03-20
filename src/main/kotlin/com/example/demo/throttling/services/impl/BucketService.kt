package com.example.demo.throttling.services.impl

import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Bucket
import com.example.demo.throttling.repositories.BucketRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.support.atomic.RedisAtomicLong
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BucketService(
	private val bucketRepository: BucketRepository,
	@Value("\${throttle.bucket_id}")
	private val bucketId: String,
	@Value("\${throttle.bucket_renovation_period}")
	private val bucketRenovationPeriod: Long,
	redisAtomicLongFactory: RedisAtomicLongFactory,
) {
	private val log = LoggerFactory.getLogger(this::class.java)

	private var lastBucketRenovation: RedisAtomicLong = redisAtomicLongFactory.of(bucketId)

	fun getBucket(): Bucket = synchronized(this) {
		bucketRepository.findByIdOrNull(bucketId)
			?: Bucket().apply {
				id = bucketId
				renovationPeriod = bucketRenovationPeriod
			}
				.also { bucketRepository.save(it) }
	}

	fun clearBucket(bucket: Bucket) {
		synchronized(this) {
			setLastBucketRenovation().also { bucketRepository.save(bucket) }
		}
	}

	private fun setLastBucketRenovation() {
		synchronized(this) {
			val currentTimestamp = System.currentTimeMillis()
			if (currentTimestamp > lastBucketRenovation.get() + bucketRenovationPeriod)
				lastBucketRenovation.set(currentTimestamp).also { log.debug("updating lastBucketRenovation timestamp") }
		}
	}

	fun getLastBucketRenovation(): Long =
		synchronized(this) { lastBucketRenovation.get() }

	override fun toString(): String =
		"Bucket(bucketId=$bucketId, bucketRenovationPeriod=$bucketRenovationPeriod)"
}
