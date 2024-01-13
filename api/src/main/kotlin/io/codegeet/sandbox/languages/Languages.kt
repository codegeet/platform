package io.codegeet.sandbox.languages

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service

enum class Language(private val id: String) {
    JAVA("java"),
    PYTHON("python");

    @JsonValue
    fun getId(): String = id
}

@Service
class Languages {
    companion object {
        val settings: Map<Language, Settings> by lazy {
            val objectMapper = jacksonObjectMapper()

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
    }

    fun getSettingsFor(language: Language): Settings {
        return settings[language] ?: throw IllegalArgumentException("Settings for '$language' not found.")
    }

    data class Settings(
        val build: String?,
        val exec: String,
        val fileName: String,
    )
}
