package com.example.demo.throttling

import com.example.demo.throttling.entities.Client

interface ThrottlingType {
    fun throttle(clientIdentifier: String)
    fun isThrottled(client: Client): Boolean
}
