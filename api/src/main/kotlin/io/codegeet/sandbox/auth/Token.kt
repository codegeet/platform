package io.codegeet.sandbox.auth

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Token(
    @Id var token: String
)
