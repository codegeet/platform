package io.codegeet.platform.config

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class LanguageConfiguration(private val objectMapper: ObjectMapper) {

    val settings: Map<Language, Settings> by lazy {
        Thread.currentThread().contextClassLoader.getResourceAsStream("languages.json")?.let {
            val map: Map<Language, Settings> = objectMapper.readValue(
                it, objectMapper.typeFactory.constructMapType(
                    Map::class.java,
                    Language::class.java,
                    Settings::class.java
                )
            )
            map
        } ?: throw IllegalStateException("Languages settings file not found.")
    }

    fun getSettingsFor(language: Language): Settings {
        return settings[language] ?: throw IllegalArgumentException("Settings for '$language' not found.")
    }

    data class Settings(
        val compile: String,
        val exec: String,
        val fileName: String,
    )
}

enum class Language(private val id: String) {
    CSHARP("csharp"),
    JAVA("java"),
    JS("js"),
    PYTHON("python"),
    TS("ts"),
    KOTLIN("kotlin");

    @JsonValue
    fun getId(): String = id
}
