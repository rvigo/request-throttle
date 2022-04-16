package com.example.demo.throttling.services

import com.example.demo.throttling.entities.Client

interface Throttling {
	fun throttle(clientIdentifier: String)
	fun Client.isThrottled(): Boolean

	infix fun Long.`is newer than`(value: Long): Boolean = this > value
	infix fun Long.`is older than`(value: Long): Boolean = this < value
}



