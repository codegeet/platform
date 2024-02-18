package io.codegeet.platform.auth

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "api_keys")
data class ApiKey(
    @Id
    val apiKey: String,

    val description: String,
    val active: Boolean,
    val dayLimit: Int
)
