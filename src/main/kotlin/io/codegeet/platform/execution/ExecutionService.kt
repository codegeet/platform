package io.codegeet.platform.execution

import com.fasterxml.jackson.databind.ObjectMapper
import io.codegeet.platform.config.Language
import io.codegeet.platform.config.LanguageConfiguration
import io.codegeet.platform.docker.DockerInput
import io.codegeet.platform.docker.DockerOutput
import io.codegeet.platform.docker.DockerService
import io.codegeet.platform.exceptions.ExecutionNotFoundException
import io.codegeet.platform.execution.api.ExecutionRequest
import io.codegeet.platform.execution.api.ExecutionStatus
import io.codegeet.platform.execution.data.Execution
import io.codegeet.platform.execution.data.ExecutionInputOutput
import io.codegeet.platform.execution.data.ExecutionsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ExecutionService(
    private val executionRepository: ExecutionsRepository,
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
        val executions = execution.executions.map {
            DockerInput.ExecutionInput(
                args = it.args?.split(" ").orEmpty(),
                stdIn = it.stdIn
            )
        }

        val output = execute(execution.language, execution.code, executions)

        execution.totalTime = Duration.between(execution.createdAt, Instant.now()).toMillis()
        execution.error = output.error
        execution.status = if (output.execCode == 1 || output.executions.any { it.execCode == 1 })
            ExecutionStatus.FAILED
        else
            ExecutionStatus.COMPLETED

        execution.executions.forEachIndexed { i, it ->
            val out = output.executions[i]
            it.stdOut = out.stdOut
            it.stdErr = out.stdErr
            it.status = if (out.execCode == 1) ExecutionStatus.FAILED else ExecutionStatus.COMPLETED
        }

        executionRepository.save(execution)
    }

    fun execute(language: Language, code: String, executions: List<DockerInput.ExecutionInput>): DockerOutput {
        val languageSettings = languageConfiguration.getSettingsFor(language)

        val input = DockerInput(
            code = code,
            fileName = languageSettings.fileName,
            instructions = DockerInput.Instructions(
                compile = languageSettings.compile,
                exec = languageSettings.exec
            ),
            executions = executions
        )

        val imageName = "codegeet/${language.getId()}:latest"

        return dockerClient.exec(imageName, objectMapper.writeValueAsString(input))
    }

    fun ExecutionRequest.toExecution(now: Instant) = Execution(
        executionId = UUID.randomUUID().toString(),
        type = this.type,
        code = this.code,
        language = this.language,
        status = ExecutionStatus.NOT_STARTED,
        createdAt = now
    ).also { execution ->
        execution.executions.addAll(this.executions.takeIf { it.isNotEmpty() }
            ?.map {
                ExecutionInputOutput(
                    executionInputOutputId = UUID.randomUUID().toString(),
                    execution = execution,
                    status = ExecutionStatus.NOT_STARTED,
                    input = objectMapper.writeValueAsString(it.input),
                    args = it.args,
                    stdIn = it.stdIn,
                )
            } ?: listOf(
            ExecutionInputOutput(
                executionInputOutputId = UUID.randomUUID().toString(),
                execution = execution,
                status = ExecutionStatus.NOT_STARTED,
                input = null,
                args = null,
                stdIn = null,
            )
        ))
    }
}
