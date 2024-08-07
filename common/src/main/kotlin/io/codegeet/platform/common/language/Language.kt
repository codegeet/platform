package io.codegeet.platform.common.language

import com.fasterxml.jackson.annotation.JsonValue

enum class Language(private val id: String) {
    CSHARP("csharp"),
    JAVA("java"),
    JS("js"),
    PYTHON("python"),
    TS("ts"),
    KOTLIN("kotlin"),
    ONESCRIPT("onescript");

    @JsonValue
    fun getId(): String = id
}