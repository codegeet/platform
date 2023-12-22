package io.codegeet.sandbox.execution

import io.codegeet.sandbox.execution.model.Execution
import org.springframework.data.repository.CrudRepository

interface ExecutionRepository : CrudRepository<Execution, String> {

}
