package io.codegeet.platform.execution

import com.fasterxml.jackson.databind.ObjectMapper
import io.codegeet.platform.config.Language
import io.codegeet.platform.config.LanguageConfiguration
import io.codegeet.platform.docker.DockerService
import io.codegeet.platform.exceptions.ExecutionNotFoundException
import io.codegeet.platform.execution.api.ExecutionRequest
import io.codegeet.platform.execution.api.ExecutionStatus
import io.codegeet.platform.execution.data.Execution
import io.codegeet.platform.execution.data.ExecutionRepository
import io.codegeet.platform.execution.data.ExecutionRun
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository,
    private val dockerClient: DockerService,
    private val languageConfiguration: LanguageConfiguration,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) {

    fun handle(request: ExecutionRequest): Execution {
        val execution = request.toExecution(Instant.now(clock).truncatedTo(ChronoUnit.MILLIS))

        executionRepository.save(execution)

        return if (request.sync == true) {
            execute(execution.executionId)
            getExecution(execution.executionId)
        } else {
            Thread { execute(execution.executionId) }.start()
            execution
        }
    }

    fun getExecution(executionId: String): Execution = executionRepository.findByIdOrNull(executionId)
        ?: throw ExecutionNotFoundException("Execution '$executionId' not found.")

    private fun execute(executionId: String) {
        val execution = getExecution(executionId)

        val output = execute(execution.language, execution.code)
        val status = if (output.stdErr.isNotEmpty() || output.stdErr.isNotEmpty()) ExecutionStatus.FAILED
        else ExecutionStatus.COMPLETED

        execution.totalTime
        execution.status = status

        execution.runs.forEach {
            it.stdOut = output.stdOut
            it.error = output.error
            it.status = status
        }

        executionRepository.save(execution)
    }

    fun execute(language: Language, code: String): DockerService.ExecutionOutput {
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

        val imageName = "codegeet/${language.getId()}:latest"

        return dockerClient.exec(imageName, objectMapper.writeValueAsString(coderunnerInput))
    }

    data class CoderunnerInput(
        val code: String,
        val args: List<String>?,
        val fileName: String,
        val instructions: Instructions
    )

    data class Instructions(
        val compile: String,
        val exec: String,
    )

    fun ExecutionRequest.toExecution(now: Instant) = Execution(
        executionId = UUID.randomUUID().toString(),
        type = this.type,
        code = this.code,
        language = this.language,
        status = ExecutionStatus.NOT_STARTED,
        createdAt = now
    ).also { execution ->
        execution.runs.addAll(this.runs.takeIf { it.isNotEmpty() }
            ?.map {
                ExecutionRun(
                    executionRunId = UUID.randomUUID().toString(),
                    execution = execution,
                    status = ExecutionStatus.NOT_STARTED,
                    input = objectMapper.writeValueAsString(it.input),
                    args = it.args,
                    stdIn = it.stdIn,
                )
            } ?: listOf(
            ExecutionRun(
                executionRunId = UUID.randomUUID().toString(),
                execution = execution,
                status = ExecutionStatus.NOT_STARTED,
                input = null,
                args = null,
                stdIn = null,
            )
        ))
    }
}
