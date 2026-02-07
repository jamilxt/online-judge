package com.onlinejudge.controller;

import com.onlinejudge.dto.ProblemResponse;
import com.onlinejudge.model.Problem;
import com.onlinejudge.model.TestCase;
import com.onlinejudge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemResponse>> getAllProblems() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getProblemById(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblemById(id));
    }

    @PostMapping
    public ResponseEntity<Problem> createProblem(@RequestBody Problem problem) {
        return ResponseEntity.ok(problemService.createProblem(problem));
    }

    @PostMapping("/{problemId}/testcases")
    public ResponseEntity<TestCase> addTestCase(
            @PathVariable Long problemId,
            @RequestBody TestCase testCase) {
        return ResponseEntity.ok(problemService.addTestCase(problemId, testCase));
    }
}
