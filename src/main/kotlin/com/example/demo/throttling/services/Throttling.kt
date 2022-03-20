package com.example.demo.throttling.services

import com.example.demo.throttling.entities.Client

interface Throttling {
    fun throttle(clientIdentifier: String)
    fun isThrottled(client: Client): Boolean
}
