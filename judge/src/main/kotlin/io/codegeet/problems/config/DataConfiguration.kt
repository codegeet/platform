package io.codegeet.problems.config

import io.codegeet.common.Language
import io.codegeet.problems.problems.model.*
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class DataConfiguration() {

    @Bean
    fun dbInit(problemsRepository: ProblemsRepository) = ApplicationRunner {
        val problemId = "two-sum"
        val problem = Problem(
            problemId = problemId,
            name = "Two Sum",
            number = 1,
            description = "Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to target.",
            difficulty = ProblemDifficulty.EASY,
            solution = ProblemSolution(
                language = Language.JAVA,
                snippet = "class Solution { public int[] twoSum(int[] nums, int target) { Map<Integer, Integer> map = new HashMap<>(); for (int i = 0; i < nums.length; i++) { int complement = target - nums[i]; if (map.containsKey(complement)) { return new int[]{map.get(complement), i}; } map.put(nums[i], i); } throw new IllegalArgumentException(\"No two sum solution\"); } }\n",
                problemId = problemId,
            ),
            metadata = Metadata(params = listOf(MetadataParam("nums"), MetadataParam("target")))
        ).also {

            it.snippets.add(
                ProblemSnippet(
                    language = Language.JAVA,
                    snippet = "class Solution {\n" +
                            "    public int[] twoSum(int[] nums, int target) {\n" +
                            "        \n" +
                            "    }\n" +
                            "}",
                    call = "toString(new Solution().twoSum(asIntArray(args[1]), asInt(args[2])))",
                )
            )

            it.cases.addAll(
                listOf(
                    ProblemCase(
                        input = "[2,7,11,15]\n9",
                        expected = "[]",
                    ),
                    ProblemCase(
                        input = "[3,2,4]\n6",
                        expected = "[]",
                    ),
                    ProblemCase(
                        input = "[2,7,11,15]\n9",
                        expected = "[]",
                    )
                )
            )
        }

        problemsRepository.save(problem)
    }
}
