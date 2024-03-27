package io.codegeet.coderunner

import io.codegeet.common.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class RunnerTest {

    private val statistics = mock<Statistics> {
        on { buildStatsCall() } doReturn emptyList()
        on { withoutMemoryStats(any()) } doAnswer { it.arguments[0] as String }
        on { getMemoryStats(any()) } doReturn 100
    }

    private val time = mock<TimeProvider> {
        on { now() } doReturn 100
    }

    private val runner = Runner(statistics, time)

    @Test
    fun run() {
        assertEquals(
            result(
                status = ExecutionJobStatus.SUCCESS,
                invocation = ExecutionJobResult.InvocationResult(status = ExecutionJobInvocationStatus.SUCCESS)
            ), runner.run(
                executionRequest("class Main { public static void main(String[] args) { }}")
            )
        )
    }

    @Test
    fun `run with stdOut output`() {
        assertEquals(
            result(
                status = ExecutionJobStatus.SUCCESS,
                invocation = ExecutionJobResult.InvocationResult(
                    status = ExecutionJobInvocationStatus.SUCCESS,
                    stdOut = "test"
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
                status = ExecutionJobStatus.SUCCESS,
                invocation = ExecutionJobResult.InvocationResult(
                    status = ExecutionJobInvocationStatus.SUCCESS,
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
                status = ExecutionJobStatus.SUCCESS,
                invocation = ExecutionJobResult.InvocationResult(
                    status = ExecutionJobInvocationStatus.SUCCESS,
                    stdOut = "test"
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
            result(
                status = ExecutionJobStatus.SUCCESS,
                invocation = ExecutionJobResult.InvocationResult(
                    status = ExecutionJobInvocationStatus.SUCCESS,
                    stdOut = "test"
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
            result(
                status = ExecutionJobStatus.INVOCATION_ERROR,
                invocation = ExecutionJobResult.InvocationResult(
                    stdErr = "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0\n\tat Main.main(Main.java:1)\n",
                    status = ExecutionJobInvocationStatus.INVOCATION_ERROR
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
                status = ExecutionJobStatus.COMPILATION_ERROR,
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
        status: ExecutionJobStatus,
        invocation: ExecutionJobResult.InvocationResult? = null,
        error: String? = null
    ) = ExecutionJobResult(
        status = status,
        invocations = invocation
            ?.let { listOf(invocation.copy(details = ExecutionJobResult.InvocationDetails(0, 100))) }
            ?: emptyList(),
        error = error,
        compilation = if (error == null) ExecutionJobResult.CompilationDetails(0, 100) else null
    )

    private fun executionRequest(code: String) = executionRequest(code, emptyList())

    private fun executionRequest(code: String, invocations: List<ExecutionJobRequest.InvocationRequest>) =
        ExecutionJobRequest(
            code = code,
            language = Language.JAVA,
            invocations = invocations
        )
}
