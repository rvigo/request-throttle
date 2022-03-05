package com.example.demo.throttling

import com.example.demo.throttling.entities.Client
import com.example.demo.throttling.repositories.ClientRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ClientService(
	private val clientRepository: ClientRepository, @Value("\${throttle.default_rate_value:5}")
	private val defaultRateValue: Int
) {
	private val log = KotlinLogging.logger {}

	fun getClientRateLimit(clientId: String): Int {
		val client = clientRepository.findById(clientId)
		return when (client.isPresent) {
			true -> client.get().rate
			false -> createNonMappedClient(clientId).also {
				log.info {
					"client not found, setting default rate to $defaultRateValue"
				}
			}
		}
	}

	private fun createNonMappedClient(clientId: String): Int {
		val client = Client(clientId, defaultRateValue)
		return clientRepository.save(client).rate
	}
}
