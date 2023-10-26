package io.codegeet.sandbox.coderunner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

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

    fun getSettingsFor(languageId: String): Settings {
        return settings[languageId] ?: throw IllegalArgumentException("Settings for '$languageId' not found.")
    }

    data class Settings(
        val build: String?,
        val run: String,
        val fileName: String,
    )
}
