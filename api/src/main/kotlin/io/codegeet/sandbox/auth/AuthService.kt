package io.codegeet.sandbox.auth

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.jvm.optionals.getOrNull

@Service
class AuthService(
    private val tokenRepository: TokenRepository
) {

    fun getToken(authorization: String?): Token =
        authorization?.let { tokenRepository.findById(authorization).getOrNull() }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Token '$authorization' does not exist.")
}