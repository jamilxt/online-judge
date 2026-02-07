package com.onlinejudge.dto;

import com.onlinejudge.model.Problem;
import com.onlinejudge.model.TestCase;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private Problem.Difficulty difficulty;
    private Integer timeLimit;
    private Integer memoryLimit;
    private LocalDateTime createdAt;
    private List<SampleTestCase> sampleTestCases;

    @Data
    @Builder
    public static class SampleTestCase {
        private String input;
        private String expectedOutput;
    }

    public static ProblemResponse from(Problem problem, List<TestCase> visibleTestCases) {
        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .timeLimit(problem.getTimeLimit())
                .memoryLimit(problem.getMemoryLimit())
                .createdAt(problem.getCreatedAt())
                .sampleTestCases(visibleTestCases.stream()
                        .map(tc -> SampleTestCase.builder()
                                .input(tc.getInput())
                                .expectedOutput(tc.getExpectedOutput())
                                .build())
                        .toList())
                .build();
    }
}
