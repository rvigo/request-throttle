package com.example.demo.throttling

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class ClientCallsHandler {
    private val tenantCalls: ConcurrentHashMap<String, AtomicLong> = ConcurrentHashMap()

    fun addClientIdentifier(clientId: String) {
        tenantCalls.putIfAbsent(clientId, AtomicLong(0))
    }

    fun incrementCount(tenantName: String) {
        tenantCalls[tenantName]?.incrementAndGet()
    }

    fun getCallsCount(tenantName: String): Long? {
        return tenantCalls[tenantName]?.get()
    }

    fun resetCount() {
        tenantCalls.replaceAll { _, _ -> AtomicLong(0) }
    }
}