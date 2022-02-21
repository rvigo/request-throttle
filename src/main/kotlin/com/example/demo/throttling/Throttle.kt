package com.example.demo.throttling

import mu.KotlinLogging
import java.util.*
import kotlin.concurrent.timerTask

class Throttle(
    private val callsHandler: ClientCallsHandler,
    private val period: Long
) {
    private val log = KotlinLogging.logger {}

    fun start() {
        Timer(true).schedule(timerTask {
            callsHandler.resetCount()
                .also { log.debug { "count reset" } }
        }, 0, period)
    }

    override fun toString(): String {
        return "Throttle(period=$period)"
    }
}