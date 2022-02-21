package com.example.demo.throttling

import com.example.demo.throttling.entity.Client
import com.example.demo.throttling.repositories.ClientRepository
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ClientService(private val clientRepository: ClientRepository) {
    private val log = KotlinLogging.logger {}
    private val DEFAULT_RATE_VALUE: Int = 5

    fun getClientRateLimit(clientId: String): Int {
        val client = clientRepository.findById(clientId)
        return when (client.isPresent) {
            true -> client.get().rate
            false -> createNonMappedClient(clientId).also {
                log.info {
                    "client not found, setting default rate to $DEFAULT_RATE_VALUE"
                }
            }
        }
    }

    private fun createNonMappedClient(clientId: String): Int {
        val client = Client(clientId, DEFAULT_RATE_VALUE)
        return clientRepository.save(client).rate
    }
}