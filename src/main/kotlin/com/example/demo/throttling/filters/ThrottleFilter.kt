package com.example.demo.throttling.filters

import com.example.demo.controllers.ControllerExceptionHandler
import com.example.demo.throttling.ClientCallsHandler
import com.example.demo.throttling.Orchestrator
import com.example.demo.throttling.exceptions.TooManyRequestsException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class ThrottleFilter(
    private val callsHandler: ClientCallsHandler,
    private val orchestrator: Orchestrator,
    private val controllerExceptionHandler: ControllerExceptionHandler
) :
    OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val clientId = request.getHeader("x-client-id")
            callsHandler.addClientIdentifier(clientId)
            orchestrator.callService(clientId)
            filterChain.doFilter(request, response)
        } catch (ex: TooManyRequestsException) {
            val objectMapper = ObjectMapper()
            response.writer.write(objectMapper.writeValueAsString(controllerExceptionHandler.tooManyRequestsHandler(ex)))
            HttpStatus.TOO_MANY_REQUESTS.value().also { response.status = it }
        }
    }
}