package io.codegeet.platform.api.executions.model

import org.springframework.data.mongodb.repository.MongoRepository

interface ExecutionRepository : MongoRepository<Execution, String>
