package io.codegeet.sandbox.coderunner

import io.codegeet.sandbox.coderunner.model.ExecutionRequest
import io.codegeet.sandbox.coderunner.model.ExecutionResult
import io.codegeet.sandbox.coderunner.model.ExecutionStatus
import io.codegeet.sandbox.coderunner.model.InvocationStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunnerTest {

    private val runner = Runner(Statistics(false))

    @Test
    fun run() {
        assertEquals(
            ExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(ExecutionResult.InvocationResult(status = InvocationStatus.SUCCESS))
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { }}")
            )
        )
    }

    @Test
    fun `run with stdOut output`() {
        assertEquals(
            ExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdOut = "test"
                    )
                )
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { System.out.print(\"test\"); }}")
            )
        )
    }

    @Test
    fun `run with stdErr output`() {
        assertEquals(
            ExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdErr = "test"
                    )
                )
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { System.err.print(\"test\"); }}")
            )
        )
    }

    @Test
    fun `run with arguments`() {
        assertEquals(
            ExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdOut = "test"
                    )
                )
            ), runner.run(
                executionRequest(
                    "class Main { public static void main(String[] args) { System.out.print(args[0]); }}",
                    listOf(ExecutionRequest.InvocationDetails(arguments = listOf("test")))
                )
            )
        )
    }
    @Test
    fun `run with stdIn`() {
        assertEquals(
            ExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdOut = "test"
                    )
                )
            ), runner.run(
                executionRequest(
                    "import java.util.Scanner; class Main { public static void main(String[] args) { System.out.print(new Scanner(System.in).nextLine()); }}",
                    listOf(ExecutionRequest.InvocationDetails(stdIn = "test"))
                )
            )
        )
    }

    @Test
    fun `run invocation code exception`() {
        assertEquals(
            ExecutionResult(
                status = ExecutionStatus.INVOCATION_ERROR,
                invocations = listOf(
                    ExecutionResult.InvocationResult(
                        stdErr = "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0\n\tat Main.main(Main.java:1)\n",
                        status = InvocationStatus.INVOCATION_ERROR
                    )
                )
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { System.out.print(args[0]); }}")
            )
        )
    }

    @Test
    fun `run invocation compilation exception`() {
        assertEquals(
            ExecutionResult(
                status = ExecutionStatus.COMPILATION_ERROR,
                error = "Main.java:1: error: not a statement\n" +
                        "class Main { public static void main(String[] args) { wft; }}\n" +
                        "                                                      ^\n" +
                        "1 error\n",
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { wft; }}")
            )
        )
    }

    private fun executionRequest(code: String) = executionRequest(code, emptyList())

    private fun executionRequest(code: String, invocations: List<ExecutionRequest.InvocationDetails>) =
        ExecutionRequest(
            code = code,
            fileName = "Main.java",
            commands = ExecutionRequest.ExecutionCommands(compilation = "javac Main.java", invocation = "java Main"),
            invocations = invocations
        )
}
