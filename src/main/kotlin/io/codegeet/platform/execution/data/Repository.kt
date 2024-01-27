package io.codegeet.platform.execution.data

import io.codegeet.platform.execution.data.Execution
import io.codegeet.platform.execution.data.ExecutionRun
import org.springframework.data.repository.CrudRepository

interface ExecutionRepository : CrudRepository<Execution, String>
interface RunRepository : CrudRepository<ExecutionRun, String>
