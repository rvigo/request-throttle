package com.example.demo.throttling

import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.example.demo.throttling.repositories.ClientIdRepository
import mu.KotlinLogging
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.stereotype.Component

@Component
class Orchestrator(
    throttle: Throttle,
    private val callsHandler: ClientCallsHandler,
    private val clientIdRepository: ClientIdRepository
) {
    private val log = KotlinLogging.logger {}

    init {
        throttle.start()
    }

    fun callService(clientId: String) {
        val count = callsHandler.getCallsCount(clientId)
        if (count!! >= clientIdRepository.getAllowedCallsValue(clientId)) {
            log.error("too many requests for {}", clientId)
            throw TooManyRequestsException("too many requests for $clientId", TOO_MANY_REQUESTS)
        }
        callsHandler.incrementCount(clientId)
        log.info("request for {} can be completed {}", clientId, count + 1)
    }
}