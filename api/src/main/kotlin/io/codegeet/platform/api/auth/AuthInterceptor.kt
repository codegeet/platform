package io.codegeet.platform.api.auth

import io.codegeet.platform.api.auth.AuthConfiguration.ApiKeyConfig
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(private val apiKeyConfig: ApiKeyConfig) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val apiKey = request.getHeader("X-API-KEY")

        if (apiKey != null && apiKey == apiKeyConfig.key) {
            return true
        }
        response.status = HttpStatus.FORBIDDEN.value()
        return false
    }
}
