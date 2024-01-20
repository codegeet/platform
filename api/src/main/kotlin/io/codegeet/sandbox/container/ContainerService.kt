package io.codegeet.sandbox.container

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.sandbox.docker.CodegeetDockerClient
import io.codegeet.sandbox.languages.Languages
import org.springframework.stereotype.Service

@Service
class ContainerService(
    private val dockerClient: CodegeetDockerClient,
    private val languages: Languages
) {

    fun run(language: String, code: String): CodegeetDockerClient.ExecutionOutput? {
        val languageSettings = languages.getSettingsFor(language)

        val coderunnerInput = CoderunnerInput(
            code = code,
            args = emptyList(),
            fileName = languageSettings.fileName,
            instructions = Instructions(
                build = languageSettings.build,
                exec = languageSettings.exec
            )
        )

        val imageName = "codegeet/${language}:latest"

        return dockerClient.exec(imageName, jacksonObjectMapper().writeValueAsString(coderunnerInput))
    }

    data class CoderunnerInput(
        @JsonProperty("code")
        val code: String,
        @JsonProperty("args")
        val args: List<String>?,
        @JsonProperty("file_name")
        val fileName: String,
        @JsonProperty("instructions")
        val instructions: Instructions
    )

    data class Instructions(
        @JsonProperty("build")
        val build: String? = null,
        @JsonProperty("exec")
        val exec: String,
    )
}
