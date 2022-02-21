package com.example.demo.throttling.configurations

import com.example.demo.throttling.ClientCallsHandler
import com.example.demo.throttling.Throttle
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ThrottleConfiguration(@Value("\${throttle.period}") val period: Long) {
    private val log = KotlinLogging.logger {}

    @Bean
    fun getThrottle(clientCallsHandler: ClientCallsHandler): Throttle {
        val throttle = Throttle(clientCallsHandler, period)
        return throttle
            .also {
                it.start()
                log.info("the throttle has started - {}", it)
            }
    }
}