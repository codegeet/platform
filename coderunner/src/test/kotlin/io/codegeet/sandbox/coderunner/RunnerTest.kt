package io.codegeet.sandbox.coderunner

import com.fasterxml.jackson.databind.ObjectMapper
import io.codegeet.sandbox.coderunner.model.ExecutionRequest
import org.junit.jupiter.api.Test

class RunnerTest {

    private val runner = Runner()

    @Test
    fun run() {
        val result = runner.run(
            ExecutionRequest(
                code = "class Main { public static void main(String[] args) { System.err.print(\"Hello, [\" + args[0] + \"]\"); }}",
                fileName = "Main.java",
                commands = ExecutionRequest.ExecutionCommands(compilation = "javac Main.java", invocation = "java Main"),
                invocations = listOf(ExecutionRequest.InvocationDetails(arguments = listOf("1000")))
            )
        )

        println(ObjectMapper().writeValueAsString(result))
    }
}
