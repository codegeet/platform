package io.codegeet.sandbox

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.codegeet.sandbox.model.Execution
import io.codegeet.sandbox.model.ExecutionRequest
import io.codegeet.sandbox.model.Language
import org.springframework.stereotype.Service
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.*

@Service
class ExecutionService {

    private val map = mutableMapOf<String, Execution?>()
    fun execute(request: ExecutionRequest): String {

        val executionId = UUID.randomUUID().toString()
        val execution = Execution(
            executionId = executionId,
            code = request.code,
            languageId = request.languageId,
            stdOut = null,
            stdErr = null,
            error = null,
            exitCode = null
        )
        map[executionId] = execution

        val fileName = when (execution.languageId) {
            Language.JAVA -> "main.java"
            Language.PYTHON -> "main.py"
        }

        val containerInput = ContainerInput(
            languageId = execution.languageId,
            files = arrayOf(ContainerInputFile(name = fileName, content = execution.code))
        )

        val process = ProcessBuilder(
            "docker run --rm -i -u codegeet -w /home/codegeet codegeet/${execution.languageId}:latest"
                .split(" ")
        )
            .start()

        val stdin = process.outputStream
        val writer = BufferedWriter(OutputStreamWriter(stdin))

        writer.write(jacksonObjectMapper().writeValueAsString(containerInput));
        writer.flush();
        writer.close();

        val exitCode = process.waitFor()

        map[executionId] = map[executionId]?.let {

            val output = jacksonObjectMapper().readValue(process.inputStream.readAsText(), ContainerOutput::class.java)

            it.copy(
                stdOut = output.stdOut,
                stdErr = output.stderr,
                error = process.errorStream.readAsText(), //todo container error
                exitCode = exitCode
            )
        }

        return executionId
    }

    fun get(executionId: String): Execution = map[executionId] ?: throw NullPointerException(" Not Found")

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
