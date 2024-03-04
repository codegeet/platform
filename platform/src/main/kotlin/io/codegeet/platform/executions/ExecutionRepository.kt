package io.codegeet.platform.executions

import io.codegeet.platform.executions.model.Execution
import org.springframework.data.repository.CrudRepository

interface ExecutionRepository : CrudRepository<Execution, String>
