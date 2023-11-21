package io.codegeet.sandbox.execution

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
class ExecutionServiceTest {

    var repository: ExecutionRepository = mock()

    val service = ExecutionService(repository)
    @Test
    fun execCmd() {

        service.execCmd("codegeet/base:latest")
    }
}