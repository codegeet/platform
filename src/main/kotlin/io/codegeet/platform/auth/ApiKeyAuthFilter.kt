package io.codegeet.platform.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.jvm.optionals.getOrNull

@Component
class ApiKeyAuthFilter(private val apiKeyRepository: ApiKeyRepository) : OncePerRequestFilter() {

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-KEY") ?: request.getParameter("key")

        if (apiKey.isNullOrEmpty() || !isValidApiKey(apiKey)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: Invalid API Key")
        }

        filterChain.doFilter(request, response)
    }

    private fun isValidApiKey(key: String): Boolean {
        return apiKeyRepository.findById(key).getOrNull()?.active ?: false
    }
}
