package io.codegeet.platform.coderunner

import io.codegeet.platform.common.*
import io.codegeet.platform.common.language.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

class RunnerTest {

    @Suppress("UNCHECKED_CAST")
    private val processStats = mock<ProcessStats> {
        on { wrapCommand(any()) } doAnswer { it.arguments[0] as List<String> }
        on { withMemory(any()) } doAnswer { Pair(it.arguments[0] as String, 100) }
        on { withTime<Process>(any()) } doAnswer {
            val block = it.arguments[0] as () -> Process
            val blockResult = block()
            Pair(blockResult, 100)
        }
    }

    private val runner = Runner(ProcessExecutor(processStats))

    @Test
    fun run() {
        assertEquals(
            result(
                status = ExecutionStatus.SUCCESS,
                invocation = ExecutionResult.InvocationResult(
                    status = InvocationStatus.SUCCESS,
                    stdOut = "",
                    stdErr = ""
                )
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { }}")
            )
        )
    }

    @Test
    fun `run with stdOut output`() {
        assertEquals(
            result(
                status = ExecutionStatus.SUCCESS,
                invocation = ExecutionResult.InvocationResult(
                    status = InvocationStatus.SUCCESS,
                    stdOut = "test",
                    stdErr = ""
                )
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { System.out.print(\"test\"); }}")
            )
        )
    }

    @Test
    fun `run with stdErr output`() {
        assertEquals(
            result(
                status = ExecutionStatus.SUCCESS,
                invocation = ExecutionResult.InvocationResult(
                    status = InvocationStatus.SUCCESS,
                    stdOut = "",
                    stdErr = "test"
                )
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { System.err.print(\"test\"); }}")
            )
        )
    }

    @Test
    fun `run with arguments`() {
        assertEquals(
            result(
                status = ExecutionStatus.SUCCESS,
                invocation = ExecutionResult.InvocationResult(
                    status = InvocationStatus.SUCCESS,
                    stdOut = "test",
                    stdErr = ""
                )
            ), runner.run(
                executionRequest(
                    "class Main { public static void main(String[] args) { System.out.print(args[0]); }}",
                    listOf(ExecutionRequest.InvocationRequest(args = listOf("test")))
                )
            )
        )
    }

    @Test
    fun `run with stdIn`() {
        assertEquals(
            result(
                status = ExecutionStatus.SUCCESS,
                invocation = ExecutionResult.InvocationResult(
                    status = InvocationStatus.SUCCESS,
                    stdOut = "test",
                    stdErr = "",
                )
            ), runner.run(
                executionRequest(
                    "import java.util.Scanner; class Main { public static void main(String[] args) { System.out.print(new Scanner(System.in).nextLine()); }}",
                    listOf(ExecutionRequest.InvocationRequest(stdIn = "test"))
                )
            )
        )
    }

    @Test
    fun `run invocation code exception`() {
        assertEquals(
            result(
                status = ExecutionStatus.INVOCATION_ERROR,
                invocation = ExecutionResult.InvocationResult(
                    status = InvocationStatus.INVOCATION_ERROR,
                    stdOut = "",
                    stdErr = "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0\n\tat Main.main(Main.java:1)\n",
                )
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { System.out.print(args[0]); }}")
            )
        )
    }

    @Test
    fun `run invocation compilation exception`() {
        assertEquals(
            result(
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

    private fun result(
        status: ExecutionStatus,
        invocation: ExecutionResult.InvocationResult? = null,
        error: String? = null
    ) = ExecutionResult(
        status = status,
        invocations = invocation
            ?.let { listOf(invocation.copy(details = ExecutionResult.Details(100, 100))) }
            ?: emptyList(),
        error = error,
        compilation = if (error == null) ExecutionResult.CompilationResult(
            ExecutionResult.Details(
                100,
                100
            )
        ) else null
    )

    private fun executionRequest(code: String) = executionRequest(code, emptyList())

    private fun executionRequest(code: String, invocations: List<ExecutionRequest.InvocationRequest>) =
        ExecutionRequest(
            code = code,
            language = Language.JAVA,
            invocations = invocations
        )
}
