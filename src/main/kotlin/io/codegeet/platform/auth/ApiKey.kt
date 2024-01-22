package io.codegeet.platform.auth

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class ApiKey(
        @Id val apiKey: String,
        val description: String,
        val active: Boolean
)
