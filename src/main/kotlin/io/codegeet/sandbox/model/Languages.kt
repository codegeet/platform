package io.codegeet.sandbox.model

import com.fasterxml.jackson.annotation.JsonValue

enum class Language(private val id: String) {
    JAVA("java");

    @JsonValue
    fun getId(): String = id
}

