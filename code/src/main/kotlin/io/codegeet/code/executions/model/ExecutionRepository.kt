package io.codegeet.code.executions.model

import org.springframework.data.repository.CrudRepository

interface ExecutionRepository : CrudRepository<Execution, String>
