package io.codegeet.problems.problems

import io.codegeet.common.Language
import io.codegeet.problems.problems.model.Problem
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/problems")
class ProblemResource(
    private val problemService: ProblemService
) {

    @GetMapping()
    fun getAll(): List<ProblemListResponse> {
        return problemService.getAll().map { it.toListResponse() }
    }

    @GetMapping("/{problem_id}")
    fun get(@PathVariable("problem_id") problemId: String): ProblemResponse {
        return problemService.get(problemId).toResponse()
    }

    data class WrappedList(
        val list: List<ProblemListResponse>
    )

    data class ProblemListResponse(
        val problemId: String,
        val name: String,
        val number: Int,
    )

    data class ProblemResponse(
        val problemId: String,
        val name: String,
        val number: Int,
        val description: String,
        val snippets: List<Snippet>,
        val cases: List<Case>,
    ) {
        data class Snippet(
            val snippet: String,
            val language: Language,
        )

        data class Case(
            val input: String
        )
    }

    private fun Problem.toListResponse() = ProblemListResponse(
        problemId = problemId,
        name = name,
        number = number
    )

    private fun Problem.toResponse() = ProblemResponse(
        problemId = problemId,
        name = name,
        number = number,
        description = description,
        snippets = snippets.map { ProblemResponse.Snippet(snippet = it.snippet, language = it.language) },
        cases = cases.map { ProblemResponse.Case(input = it.input) }
    )
}
