package io.codegeet.platform.execution

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.platform.config.LanguageConfiguration
import io.codegeet.platform.docker.DockerService
import io.codegeet.platform.exceptions.ExecutionNotFoundException
import io.codegeet.platform.execution.model.Execution
import io.codegeet.platform.execution.model.ExecutionRequest
import io.codegeet.platform.execution.model.ExecutionResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class ExecutionService(
        private val executionRepository: ExecutionRepository,
        private val dockerClient: DockerService,
        private val languageConfiguration: LanguageConfiguration
) {

    fun handleExecutionRequest(request: ExecutionRequest): ExecutionResponse {
        val executionId = UUID.randomUUID().toString()

        executionRepository.save(
            Execution(
                executionId = executionId,
                code = request.code,
                language = request.language,
                stdOut = null,
                stdErr = null,
                error = null,
                exitCode = null,
                createdAt = Instant.now()
            )
        )

        return if (request.immediately == true) {
            executeImmediately(executionId)
            ExecutionResponse(executionId = executionId, execution = getExecution(executionId))
        } else {
            Thread { executeImmediately(executionId) }.start()
            ExecutionResponse(executionId = executionId, execution = null)
        }
    }

    fun getExecution(executionId: String): Execution = executionRepository.findByIdOrNull(executionId)
        ?: throw ExecutionNotFoundException("Execution '$executionId' not found.")

    private fun executeImmediately(executionId: String) {
        val execution = getExecution(executionId)

        val output = execute(execution.language, execution.code)

        val executedAt = Instant.now()
        executionRepository.save(
            execution.copy(
                stdOut = output?.stdOut,
                stdErr = output?.stdErr,
                error = output?.error,
                executedAt = executedAt,
                totalExecutionMillis = executedAt.toEpochMilli() - execution.createdAt.toEpochMilli(),
                exitCode = if (output?.stdErr?.isNotEmpty() == true) 1 else 0
            )
        )
    }

    fun execute(language: String, code: String): DockerService.ExecutionOutput? {
        val languageSettings = languageConfiguration.getSettingsFor(language)

        val coderunnerInput = CoderunnerInput(
            code = code,
            args = emptyList(),
            fileName = languageSettings.fileName,
            instructions = Instructions(
                compile = languageSettings.compile,
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
        @JsonProperty("compile")
        val compile: String,
        @JsonProperty("exec")
        val exec: String,
    )
}
