package io.codegeet.platform.problems

import io.codegeet.common.Language
import io.codegeet.platform.problems.data.Problem
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("api/problems")
class ProblemResource(
    private val problemService: ProblemService
) {

    @GetMapping("/list")
    @ResponseBody
    fun getAll(): WrappedList {
        return WrappedList(list = problemService.getAll().map { it.toListResponse() })
    }

    @GetMapping("/{problem_id}")
    @ResponseBody
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
        val inputs: List<Input>,
    ) {
        data class Snippet(
            val snippet: String,
            val language: Language,
        )

        data class Input(
            val input: String,
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
        inputs = cases.map { ProblemResponse.Input(input = it.input) }
    )
}
