package io.codegeet.sandbox

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

        val containerInput = ContainerInput(
            language = execution.languageId,
            files = arrayOf(ContainerInputFile(name = "Main.java", content = execution.code))
        )

        val process = ProcessBuilder("docker run --rm -i -u codegeet -w /home/codegeet codegeet".split(" "))
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
                stdOut = output.stdout,
                stdErr = output.stderr,
                error = process.errorStream.readAsText(), //todo container error
                exitCode = exitCode
            )
        }

        return executionId
    }

    fun get(executionId: String): Execution = map[executionId] ?: throw NullPointerException(" Not Found")

    data class ContainerInput(
        val language: Language,
        val files: Array<ContainerInputFile>
    )

    data class ContainerInputFile(
        var name: String,
        var content: String,
    )

    data class ContainerOutput(
        val stdout: String,
        val stderr: String,
        val error: String,
    )
}
