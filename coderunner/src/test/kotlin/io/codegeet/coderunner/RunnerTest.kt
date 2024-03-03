package io.codegeet.coderunner

import io.codegeet.common.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunnerTest {

    private val runner = Runner(Statistics(false))

    @Test
    fun run() {
        assertEquals(
            ContainerExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(ContainerExecutionResult.InvocationResult(status = InvocationStatus.SUCCESS))
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { }}")
            )
        )
    }

    @Test
    fun `run with stdOut output`() {
        assertEquals(
            ContainerExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ContainerExecutionResult.InvocationResult(
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
            ContainerExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ContainerExecutionResult.InvocationResult(
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
            ContainerExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ContainerExecutionResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdOut = "test"
                    )
                )
            ), runner.run(
                executionRequest(
                    "class Main { public static void main(String[] args) { System.out.print(args[0]); }}",
                    listOf(ContainerExecutionRequest.InvocationDetails(arguments = listOf("test")))
                )
            )
        )
    }

    @Test
    fun `run with stdIn`() {
        assertEquals(
            ContainerExecutionResult(
                status = ExecutionStatus.SUCCESS,
                invocations = listOf(
                    ContainerExecutionResult.InvocationResult(
                        status = InvocationStatus.SUCCESS,
                        stdOut = "test"
                    )
                )
            ), runner.run(
                executionRequest(
                    "import java.util.Scanner; class Main { public static void main(String[] args) { System.out.print(new Scanner(System.in).nextLine()); }}",
                    listOf(ContainerExecutionRequest.InvocationDetails(stdIn = "test"))
                )
            )
        )
    }

    @Test
    fun `run invocation code exception`() {
        assertEquals(
            ContainerExecutionResult(
                status = ExecutionStatus.INVOCATION_ERROR,
                invocations = listOf(
                    ContainerExecutionResult.InvocationResult(
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
            ContainerExecutionResult(
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

    private fun executionRequest(code: String, invocations: List<ContainerExecutionRequest.InvocationDetails>) =
        ContainerExecutionRequest(
            code = code,
            language = Language.JAVA,
            invocations = invocations
        )
}
