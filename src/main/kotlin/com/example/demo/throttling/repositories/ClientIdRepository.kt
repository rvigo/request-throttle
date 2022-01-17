package com.example.demo.throttling.repositories
import mu.KotlinLogging
import org.springframework.stereotype.Component

// database mock
@Component
class ClientIdRepository {
    private val log = KotlinLogging.logger {}
    private val clientIdMap: HashMap<String, Int> = HashMap()
    private val DEFAULT_ALLOWED_VALUE: Int = 5

    init {
        clientIdMap["client_1"] = 7
        clientIdMap["client_2"] = 10
    }

    fun getAllowedCallsValue(clientId: String): Int {
        if (!clientIdMap.containsKey(clientId)) {
            log.warn("creating new entry for clientId {}", clientId)
            clientIdMap[clientId] = DEFAULT_ALLOWED_VALUE
            return DEFAULT_ALLOWED_VALUE
        }
        return clientIdMap[clientId]!!
    }
}