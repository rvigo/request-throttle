package com.example.demo.configurations

import com.example.demo.throttling.configurations.RedisAtomicLongFactory
import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.impl.FixedWindowCounterThrottle
import com.example.demo.throttling.repositories.ClientRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ThrottleConfiguration {
	private val log = LoggerFactory.getLogger(this::class.java)

/*	@Bean
	fun getThrottle(
		clientRepository: ClientRepository,
		@Value("\${throttle.period}") period: Long,
		@Value("\${throttle.default_rate_value}") defaultRateValue: Int,
		redisAtomicLongFactory: RedisAtomicLongFactory
	): LeakingBucketThrottling {
		val client1 = Client(name = "test", rate = 50)
		val client2 = Client(name = "test-2", rate = 120)

		log.info("creating LeakingBucket object")
		return LeakingBucketThrottling(
			clientRepository = clientRepository,
			redisAtomicLongFactory = redisAtomicLongFactory,
			bucketRenovationPeriod = 10L,
			defaultRateValue = defaultRateValue
		).also {
			it.addClient(client1)
			it.addClient(client2)
			log.info(it.toString())
		}
	}*/

	@Bean
	fun getThrottle(
		clientRepository: ClientRepository,
		@Value("\${throttle.period}") period: Long,
		@Value("\${throttle.default_rate_value}") defaultRateValue: Int,
		redisAtomicLongFactory: RedisAtomicLongFactory
	): FixedWindowCounterThrottle {
		val client1 = Client(name = "test", rate = 7)
		val client2 = Client(name = "test-2", rate = 2)

		log.info("creating LeakingBucket object")
		return FixedWindowCounterThrottle(
			clientRepository = clientRepository,
			period = period,
			defaultRateValue = defaultRateValue,
			redisAtomicLongFactory = redisAtomicLongFactory
		).also {
			it.addClient(client1)
			it.addClient(client2)
			log.info(it.toString())
		}
	}
}
