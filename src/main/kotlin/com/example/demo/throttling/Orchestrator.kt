package com.example.demo.throttling

import com.example.demo.throttling.exceptions.TooManyRequestsException
import mu.KotlinLogging
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.stereotype.Component

@Component
class Orchestrator(
    private val callsHandler: ClientCallsHandler,
    private val clientService: ClientService,
) {


    private val log = KotlinLogging.logger {}

    fun callService(clientId: String) {
        val count = callsHandler.getCallsCount(clientId)
        if (count!! >= clientService.getClientRateLimit(clientId)) {
            log.error("too many requests for {}", clientId)
            throw TooManyRequestsException("too many requests for $clientId", TOO_MANY_REQUESTS)
        }

        callsHandler.incrementCount(clientId)
            .also { log.info("request for {} can be completed {}", clientId, count + 1) }

    }
}