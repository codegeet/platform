package io.codegeet.platform.executions.model

import io.codegeet.platform.executions.model.Execution
import org.springframework.data.repository.CrudRepository

interface CodeExecutionRepository : CrudRepository<Execution, String>
