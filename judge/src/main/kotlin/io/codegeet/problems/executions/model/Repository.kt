package io.codegeet.problems.executions.model

import org.springframework.data.mongodb.repository.MongoRepository

interface ExecutionRepository : MongoRepository<Execution, String>
