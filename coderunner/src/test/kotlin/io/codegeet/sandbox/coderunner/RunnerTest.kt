package io.codegeet.sandbox.coderunner

import io.codegeet.sandbox.coderunner.model.ApplicationInput
import io.codegeet.sandbox.coderunner.model.Instructions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RunnerTest {

    private val runner = Runner()

    @Test
    fun run() {
        val result = runner.run(
            ApplicationInput(
                code = "",
                args = emptyList(),
                fileName = "file.txt",
                instructions = Instructions(exec = "echo Hello, world!")
            )
        )

        assertEquals("Hello, world!\n", result.stdOut)
        assertEquals("", result.stdErr)
        assertEquals("", result.error)
        assertEquals(0, result.execCode)
    }
}
