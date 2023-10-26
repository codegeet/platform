package io.codegeet.sandbox.execution

import org.springframework.data.repository.CrudRepository

interface ExecutionRepository : CrudRepository<Execution, String> {

}
