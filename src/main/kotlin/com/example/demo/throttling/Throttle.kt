package com.example.demo.throttling

import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.timerTask

@Component
class Throttle(private val callsHandler: ClientCallsHandler, private val period: Long = 1000) {
    fun start() {
        Timer(true).schedule(timerTask {
            callsHandler.resetCount();
        }, 0, period)
    }
}