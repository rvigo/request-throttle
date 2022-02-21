package com.example.demo.throttling.filters

import com.example.demo.dtos.DataWrapper
import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.fasterxml.jackson.databind.ObjectMapper

class FilterErrorHandler {
    companion object {
        private val objectMapper = ObjectMapper()

        fun handle(ex: TooManyRequestsException): String =
            objectMapper.writeValueAsString(DataWrapper(ex.message))
    }
}