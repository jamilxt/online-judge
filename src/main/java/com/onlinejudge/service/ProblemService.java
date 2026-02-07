package com.onlinejudge.service;

import com.onlinejudge.dto.ProblemResponse;
import com.onlinejudge.model.Problem;
import com.onlinejudge.model.TestCase;
import com.onlinejudge.repository.ProblemRepository;
import com.onlinejudge.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public List<ProblemResponse> getAllProblems() {
        return problemRepository.findAllByOrderByIdAsc()
                .stream()
                .map(p -> ProblemResponse.from(p, List.of()))
                .toList();
    }

    public ProblemResponse getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
        
        List<TestCase> visibleTestCases = testCaseRepository
                .findByProblemIdAndIsHiddenFalseOrderByOrderIndexAsc(id);
        
        return ProblemResponse.from(problem, visibleTestCases);
    }

    public Problem createProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public TestCase addTestCase(Long problemId, TestCase testCase) {
        problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemId));
        testCase.setProblemId(problemId);
        return testCaseRepository.save(testCase);
    }
}
