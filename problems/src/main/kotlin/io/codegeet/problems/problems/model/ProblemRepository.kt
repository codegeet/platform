package io.codegeet.problems.problems.model

import org.springframework.data.repository.CrudRepository

interface ProblemsRepository : CrudRepository<Problem, String>
interface SnippetRepository : CrudRepository<ProblemSnippet, String>
