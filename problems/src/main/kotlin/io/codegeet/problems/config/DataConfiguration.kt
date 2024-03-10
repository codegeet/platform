package io.codegeet.problems.config

import io.codegeet.common.Language
import io.codegeet.problems.problems.model.*
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class DataConfiguration(private val environment: Environment) {

    @Bean
    fun dbInit(problemsRepository: ProblemsRepository, snippetRepository: SnippetRepository) = ApplicationRunner {
        val problemId = "two-sum"
        val problem = Problem(
            problemId = problemId,
            name = "Two Sum",
            number = 1,
            description = "Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to target.",
            difficulty = ProblemDifficulty.EASY,
            solution = ProblemSolution(
                solutionId = "solutionId",
                language = Language.JAVA,
                snippet = "class Solution { public int[] twoSum(int[] nums, int target) { Map<Integer, Integer> map = new HashMap<>(); for (int i = 0; i < nums.length; i++) { int complement = target - nums[i]; if (map.containsKey(complement)) { return new int[]{map.get(complement), i}; } map.put(nums[i], i); } throw new IllegalArgumentException(\"No two sum solution\"); } }\n",
                problemId = problemId,
            )
        ).also {

            it.snippets.add(
                ProblemSnippet(
                    snippetId = "snippetId",
                    language = Language.JAVA,
                    snippet = "class Solution {\n" +
                            "    public int[] twoSum(int[] nums, int target) {\n" +
                            "        \n" +
                            "    }\n" +
                            "}",
                    call = "toString(new Solution().twoSum(asIntArray(args[1]), asInt(args[2])))",
                    problemId = problemId
                )
            )

            it.cases.addAll(
                listOf(
                    ProblemCase(
                        caseId = "case1",
                        input = "[2,7,11,15]\n9",
                        expected = "[]",
                        problemId = problemId
                    ),
                    ProblemCase(
                        caseId = "case2",
                        input = "[3,2,4]\n6",
                        expected = "[]",
                        problemId = problemId
                    ),
                    ProblemCase(
                        caseId = "case3",
                        input = "[2,7,11,15]\n9",
                        expected = "[]",
                        problemId = problemId
                    )
                )
            )
        }

        problemsRepository.save(problem)
    }
}
