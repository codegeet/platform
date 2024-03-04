package io.codegeet.platform.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.jvm.optionals.getOrNull

@Component
class AuthFilter(private val apiKeyRepository: ApiKeyRepository) : OncePerRequestFilter() {

    @Value("\${app.auth.enabled:true}")
    private lateinit var enabled: String

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (enabled.toBoolean() && !isValid(getApiKey(request))) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: Invalid API Key")
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun getApiKey(request: HttpServletRequest): String? =
        request.getHeader("X-API-KEY") ?: request.getParameter("key")

    private fun isValid(apiKey: String?): Boolean {
        return apiKey
            ?.takeIf { it.isNotEmpty() }
            ?.let { apiKeyRepository.findById(apiKey).getOrNull()?.active ?: false } ?: false
    }
}
