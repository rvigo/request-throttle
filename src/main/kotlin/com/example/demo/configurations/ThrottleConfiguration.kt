package com.example.demo.configurations

import com.example.demo.throttling.services.impl.BucketService
import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.services.impl.SlidingWindowCounterThrottle
import com.example.demo.throttling.repositories.ClientRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ThrottleConfiguration {
	private val log = LoggerFactory.getLogger(this::class.java)

	@Bean
	fun getThrottle(
		clientRepository: ClientRepository,
		@Value("\${throttle.default_rate_value}") defaultRateValue: Long,
		redisAtomicLongFactory: RedisAtomicLongFactory,
		bucketService: BucketService
	): SlidingWindowCounterThrottle {

		log.info("populating database...")
		populateDatabase(clientRepository)

		log.info("creating SlidingWindowCounterThrottle object")
		return SlidingWindowCounterThrottle(
			clientRepository = clientRepository,
			redisAtomicLongFactory = redisAtomicLongFactory,
			defaultRateValue = defaultRateValue,
			bucketService = bucketService
		)
	}

	fun populateDatabase(clientRepository: ClientRepository) {
		val client1 = Client(name = "key1", rate = 50, isMapped = true)
		val client2 = Client(name = "key2", rate = 120, isMapped = true)
		val clients = clientRepository.findAll()
		when (clients.contains(client1) && clients.contains(client2)) {
			false -> clientRepository.saveAll(listOf(client1, client2)).also { log.info("populating clients") }
			true -> log.info("mapped clients already in database, nothing to do")
		}
	}

//	@Bean
//	fun getThrottle(
//		clientRepository: ClientRepository,
//		@Value("\${throttle.period}") period: Long,
//		@Value("\${throttle.default_rate_value}") defaultRateValue: Int,
//		redisAtomicLongFactory: RedisAtomicLongFactory
//	): FixedWindowCounterThrottle {
//		val client1 = Client(name = "test", rate = 7)
//		val client2 = Client(name = "test-2", rate = 2)
//
//		log.info("creating LeakingBucket object")
//		return FixedWindowCounterThrottle(
//			clientRepository = clientRepository,
//			period = period,
//			defaultRateValue = defaultRateValue,
//			redisAtomicLongFactory = redisAtomicLongFactory
//		).also {
//			it.addClient(client1)
//			it.addClient(client2)
//			log.info(it.toString())
//		}
//	}
}
