package io.codegeet.platform.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

@Component
class RateLimitFilter(private val apiKeyRepository: ApiKeyRepository) : OncePerRequestFilter() {

    @Value("\${app.rate-limit.enabled:true}")
    private lateinit var rateLimitEnabled: String

    @Value("\${app.auth.enabled:true}")
    private lateinit var authEnabled: String

    private val rateLimitByKey = ConcurrentHashMap<String, RateLimit>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (authEnabled.toBoolean() && rateLimitEnabled.toBoolean()) {
            if (getApiKey(request)?.let { isAllowed(it) } == false) {
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun getApiKey(request: HttpServletRequest): String? =
        request.getHeader("X-API-KEY") ?: request.getParameter("key")

    fun isAllowed(apiKey: String): Boolean {
        val today = LocalDate.now()
        val rateLimit = rateLimitByKey.compute(apiKey) { _, currentInfo ->
            if (currentInfo == null || currentInfo.date != today) {
                RateLimit(today, getDayLimit(apiKey))
            } else if (currentInfo.count > 0) {
                RateLimit(currentInfo.date, currentInfo.count - 1)
            } else {
                currentInfo
            }
        }

        return (rateLimit?.count ?: 0) > 0
    }

    private fun getDayLimit(apiKey: String) = apiKeyRepository.findById(apiKey).getOrNull()?.dayLimit ?: 0

    data class RateLimit(val date: LocalDate, val count: Int)
}
