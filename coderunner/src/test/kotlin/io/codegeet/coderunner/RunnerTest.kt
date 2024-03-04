package io.codegeet.coderunner

import io.codegeet.common.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunnerTest {

    private val runner = Runner(Statistics())

    @Test
    fun run() {
        assertEquals(
            ExecutionJobResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(ExecutionJobResult.InvocationResult(status = InvocationStatus.SUCCESS))
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { }}")
            )
        )
    }

    @Test
    fun `run with stdOut output`() {
        assertEquals(
            ExecutionJobResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionJobResult.InvocationResult(
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
            ExecutionJobResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionJobResult.InvocationResult(
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
            ExecutionJobResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionJobResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdOut = "test"
                    )
                )
            ), runner.run(
                executionRequest(
                    "class Main { public static void main(String[] args) { System.out.print(args[0]); }}",
                    listOf(ExecutionJobRequest.InvocationRequest(arguments = listOf("test")))
                )
            )
        )
    }

    @Test
    fun `run with stdIn`() {
        assertEquals(
            ExecutionJobResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ExecutionJobResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdOut = "test"
                    )
                )
            ), runner.run(
                executionRequest(
                    "import java.util.Scanner; class Main { public static void main(String[] args) { System.out.print(new Scanner(System.in).nextLine()); }}",
                    listOf(ExecutionJobRequest.InvocationRequest(stdIn = "test"))
                )
            )
        )
    }

    @Test
    fun `run invocation code exception`() {
        assertEquals(
            ExecutionJobResult(
                status = ExecutionStatus.INVOCATION_ERROR,
                invocations = listOf(
                    ExecutionJobResult.InvocationResult(
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
            ExecutionJobResult(
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

    private fun executionRequest(code: String, invocations: List<ExecutionJobRequest.InvocationRequest>) =
        ExecutionJobRequest(
            code = code,
            language = Language.JAVA,
            invocations = invocations,
            stats = false,
        )
}
