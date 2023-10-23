package io.codegeet.sandbox.execution

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.sandbox.model.ExecutionRequest
import io.codegeet.sandbox.model.ExecutionResponse
import io.codegeet.sandbox.model.Language
import io.codegeet.sandbox.readAsText
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.*

@Service
class ExecutionService(
    private val executionRepository: ExecutionRepository
) {

    fun execute(request: ExecutionRequest): ExecutionResponse {

        val execution = Execution(
            executionId = UUID.randomUUID().toString(),
            code = request.code,
            languageId = request.languageId,
            stdOut = null,
            stdErr = null,
            error = null,
            exitCode = null
        )

        executionRepository.save(execution)

        Thread {
            executeAsync(execution.executionId)
        }.start()

        return ExecutionResponse(executionId = execution.executionId.toString())
    }

    fun executeAsync(executionId: String) {

        val execution = executionRepository.findByIdOrNull(executionId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Execution '$executionId' does not exist."
        )

        val fileName = when (execution.languageId) {
            Language.JAVA -> "main.java"
            Language.PYTHON -> "main.py"
        }

        val containerInput = ContainerInput(
            languageId = execution.languageId,
            files = arrayOf(ContainerInputFile(name = fileName, content = execution.code))
        )

        val process = ProcessBuilder(
            "docker run --rm -i -u codegeet -w /home/codegeet codegeet/${execution.languageId.getId()}:latest"
                .split(" ")
        )
            .start()

        val stdin = process.outputStream
        val writer = BufferedWriter(OutputStreamWriter(stdin))

        writer.write(jacksonObjectMapper().writeValueAsString(containerInput));
        writer.flush();
        writer.close();

        val exitCode = process.waitFor()

        executionRepository.save(execution.let {
            val output =
                jacksonObjectMapper().readValue(process.inputStream.readAsText(), ContainerOutput::class.java)

            it.copy(
                stdOut = output.stdOut,
                stdErr = output.stderr,
                error = process.errorStream.readAsText(), //todo container error
                exitCode = exitCode
            )
        })
    }

    fun get(executionId: String): Execution? = executionRepository.findByIdOrNull(executionId)

    data class ContainerInput(
        @JsonProperty("language_id")
        val languageId: Language,
        val files: Array<ContainerInputFile>
    )

    data class ContainerInputFile(
        var name: String,
        var content: String,
    )

    data class ContainerOutput(
        @JsonProperty("std_out")
        val stdOut: String,
        @JsonProperty("std_err")
        val stderr: String,
        val error: String,
    )
}
