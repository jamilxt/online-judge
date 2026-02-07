package com.onlinejudge.controller;

import com.onlinejudge.dto.SubmissionRequest;
import com.onlinejudge.dto.SubmissionResponse;
import com.onlinejudge.service.JudgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final JudgeService judgeService;

    @PostMapping
    public ResponseEntity<SubmissionResponse> submitCode(@Valid @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(judgeService.submitCode(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(judgeService.getSubmission(id));
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsForProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(judgeService.getSubmissionsForProblem(problemId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SubmissionResponse>> getRecentSubmissions() {
        return ResponseEntity.ok(judgeService.getRecentSubmissions());
    }
}
