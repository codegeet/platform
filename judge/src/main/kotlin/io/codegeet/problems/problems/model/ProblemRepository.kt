package io.codegeet.problems.problems.model

import org.springframework.data.mongodb.repository.MongoRepository

interface ProblemsRepository : MongoRepository<Problem, String>
