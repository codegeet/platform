package io.codegeet.sandbox.execution

import org.springframework.data.repository.CrudRepository
import java.util.*

interface ExecutionRepository : CrudRepository<Execution, String> {

}
