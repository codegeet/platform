package io.codegeet.platform.execution

import io.codegeet.platform.execution.model.Execution
import org.springframework.data.repository.CrudRepository

interface ExecutionRepository : CrudRepository<Execution, String> {

}
