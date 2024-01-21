package io.codegeet.sandbox.languages

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service

@Service
class Languages {
    companion object {
        val settings: Map<String, Settings> by lazy {
            val objectMapper = jacksonObjectMapper()

            Thread.currentThread().contextClassLoader.getResourceAsStream("languages.json")?.let {
                val map: Map<String, Settings> = objectMapper.readValue(
                    it, objectMapper.typeFactory.constructMapType(
                        Map::class.java,
                        String::class.java,
                        Settings::class.java
                    )
                )
                map
            } ?: throw IllegalStateException("Languages settings file not found.")
        }
    }

    fun getSettingsFor(language: String): Settings {
        return settings[language] ?: throw IllegalArgumentException("Settings for '$language' not found.")
    }

    data class Settings(
        val compile: String,
        val exec: String,
        @JsonProperty("file_name")
        val fileName: String,
    )
}
