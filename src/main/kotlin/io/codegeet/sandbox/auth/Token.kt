package io.codegeet.sandbox.auth

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.util.*

@Entity
data class Token(
    @Id var uuid: UUID
)
