package com.example.demo.throttling.filters

import com.example.demo.throttling.ClientCallsHandler
import com.example.demo.throttling.Orchestrator
import com.example.demo.throttling.exceptions.TooManyRequestsException
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class ThrottleFilter(
	private val callsHandler: ClientCallsHandler,
	private val orchestrator: Orchestrator,
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
			prepareErrorResponse(ex, response)
		}
	}

	private fun prepareErrorResponse(ex: TooManyRequestsException, response: HttpServletResponse) {
		val json = FilterErrorHandler.handle(ex)
		response.run {
			writer.write(json)
			contentType = MediaType.APPLICATION_JSON_VALUE
			status = ex.httpStatus.value()
		}
	}
}
