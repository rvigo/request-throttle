package com.example.demo.throttling

import java.util.*
import kotlin.concurrent.timerTask

class Throttle(
    private val callsHandler: ClientCallsHandler,
    private val period: Long
) {
    fun start() {
        Timer(true).schedule(timerTask {
            callsHandler.resetCount()
        }, 0, period)
    }

    override fun toString(): String = "Throttle(period=$period)"
}
