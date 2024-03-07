package io.codegeet.platform.problems.data

import org.springframework.data.repository.CrudRepository

interface ProblemsRepository : CrudRepository<Problem, String>
interface SnippetRepository : CrudRepository<ProblemSnippet, String>
