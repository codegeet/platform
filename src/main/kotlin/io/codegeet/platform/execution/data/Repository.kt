package io.codegeet.platform.execution.data

import org.springframework.data.repository.CrudRepository

interface ExecutionsRepository : CrudRepository<Execution, String>
interface ExecutionsInputOutputRepository : CrudRepository<ExecutionInputOutput, String>
