package com.onlinejudge.service;

import com.onlinejudge.dto.SubmissionRequest;
import com.onlinejudge.dto.SubmissionResponse;
import com.onlinejudge.model.Problem;
import com.onlinejudge.model.Submission;
import com.onlinejudge.model.TestCase;
import com.onlinejudge.repository.ProblemRepository;
import com.onlinejudge.repository.SubmissionRepository;
import com.onlinejudge.repository.TestCaseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgeService {

    private final CodeExecutor codeExecutor;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;

    // Language ID to name mapping
    private static final Map<Integer, String> LANGUAGE_NAMES = Map.of(
            71, "Python 3",
            62, "Java",
            54, "C++ (GCC)",
            63, "JavaScript (Node.js)",
            50, "C (GCC)"
    );

    @PostConstruct
    public void init() {
        log.info("JudgeService initialized with executor: {}", codeExecutor.getExecutorType());
    }

    public SubmissionResponse submitCode(SubmissionRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + request.getProblemId()));

        // Create pending submission
        Submission submission = Submission.builder()
                .problemId(problem.getId())
                .languageId(request.getLanguageId())
                .languageName(LANGUAGE_NAMES.getOrDefault(request.getLanguageId(), "Unknown"))
                .sourceCode(request.getSourceCode())
                .verdict(Submission.Verdict.PENDING)
                .build();
        submission = submissionRepository.save(submission);

        // Validate language support
        if (!LANGUAGE_NAMES.containsKey(request.getLanguageId())) {
            submission.setVerdict(Submission.Verdict.INTERNAL_ERROR);
            submission.setErrorMessage("Unsupported language. Supported: Python, Java, C++, JavaScript, C");
            submissionRepository.save(submission);
            return buildResponse(submission, problem, new ArrayList<>());
        }

        // Get all test cases for this problem
        List<TestCase> testCases = testCaseRepository.findByProblemIdOrderByOrderIndexAsc(problem.getId());
        
        if (testCases.isEmpty()) {
            submission.setVerdict(Submission.Verdict.INTERNAL_ERROR);
            submission.setErrorMessage("No test cases found for this problem");
            submissionRepository.save(submission);
            return buildResponse(submission, problem, new ArrayList<>());
        }

        // Run against each test case
        List<SubmissionResponse.TestCaseResult> testCaseResults = new ArrayList<>();
        Submission.Verdict finalVerdict = Submission.Verdict.ACCEPTED;
        long maxTime = 0;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            
            CodeExecutor.ExecutionResult result = codeExecutor.execute(
                    request.getSourceCode(),
                    request.getLanguageId(),
                    tc.getInput(),
                    problem.getTimeLimit(),
                    problem.getMemoryLimit()
            );

            // Track execution time
            if (result.executionTimeMs > maxTime) {
                maxTime = result.executionTimeMs;
            }

            // Handle different result statuses
            switch (result.status) {
                case COMPILATION_ERROR:
                    submission.setVerdict(Submission.Verdict.COMPILATION_ERROR);
                    submission.setCompileOutput(result.stderr);
                    submissionRepository.save(submission);
                    return buildResponse(submission, problem, testCaseResults);
                    
                case TIME_LIMIT_EXCEEDED:
                    finalVerdict = Submission.Verdict.TIME_LIMIT_EXCEEDED;
                    addTestCaseResult(testCaseResults, i + 1, false, "", tc, result, true);
                    submission.setVerdict(finalVerdict);
                    submission.setExecutionTime((double) maxTime / 1000);
                    submissionRepository.save(submission);
                    return buildResponse(submission, problem, testCaseResults);

                case MEMORY_LIMIT_EXCEEDED:
                    finalVerdict = Submission.Verdict.MEMORY_LIMIT_EXCEEDED;
                    addTestCaseResult(testCaseResults, i + 1, false, "", tc, result, true);
                    submission.setVerdict(finalVerdict);
                    submission.setExecutionTime((double) maxTime / 1000);
                    submissionRepository.save(submission);
                    return buildResponse(submission, problem, testCaseResults);
                    
                case RUNTIME_ERROR:
                    finalVerdict = Submission.Verdict.RUNTIME_ERROR;
                    submission.setErrorMessage(result.stderr);
                    addTestCaseResult(testCaseResults, i + 1, false, result.stdout, tc, result, true);
                    submission.setVerdict(finalVerdict);
                    submission.setExecutionTime((double) maxTime / 1000);
                    submissionRepository.save(submission);
                    return buildResponse(submission, problem, testCaseResults);
                    
                case INTERNAL_ERROR:
                    submission.setVerdict(Submission.Verdict.INTERNAL_ERROR);
                    submission.setErrorMessage(result.errorMessage);
                    submissionRepository.save(submission);
                    return buildResponse(submission, problem, testCaseResults);
                    
                case SUCCESS:
                    // Check output
                    String actualOutput = normalizeOutput(result.stdout);
                    String expectedOutput = normalizeOutput(tc.getExpectedOutput());
                    boolean passed = actualOutput.equals(expectedOutput);
                    
                    addTestCaseResult(testCaseResults, i + 1, passed, actualOutput, tc, result, false);
                    
                    if (!passed) {
                        finalVerdict = Submission.Verdict.WRONG_ANSWER;
                        break; // Stop on first failure
                    }
                    break;
                    
                default:
                    break;
            }
            
            // Stop if we got a wrong answer
            if (finalVerdict == Submission.Verdict.WRONG_ANSWER) {
                break;
            }
        }

        // Update and save final submission
        submission.setVerdict(finalVerdict);
        submission.setExecutionTime((double) maxTime / 1000);
        submissionRepository.save(submission);

        return buildResponse(submission, problem, testCaseResults);
    }

    private void addTestCaseResult(List<SubmissionResponse.TestCaseResult> results, 
                                    int num, boolean passed, String actualOutput, 
                                    TestCase tc, CodeExecutor.ExecutionResult result,
                                    boolean isError) {
        results.add(SubmissionResponse.TestCaseResult.builder()
                .testCaseNumber(num)
                .passed(passed)
                .actualOutput(tc.getIsHidden() ? "[Hidden]" : (isError ? result.stderr : actualOutput))
                .expectedOutput(tc.getIsHidden() ? "[Hidden]" : tc.getExpectedOutput())
                .executionTime((double) result.executionTimeMs / 1000)
                .memoryUsed(0)
                .hidden(tc.getIsHidden())
                .build());
    }

    public SubmissionResponse getSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
        
        Problem problem = problemRepository.findById(submission.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        
        return buildResponse(submission, problem, new ArrayList<>());
    }

    public List<SubmissionResponse> getSubmissionsForProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + problemId));
        
        return submissionRepository.findByProblemIdOrderBySubmittedAtDesc(problemId)
                .stream()
                .map(s -> buildResponse(s, problem, new ArrayList<>()))
                .toList();
    }

    public List<SubmissionResponse> getRecentSubmissions() {
        return submissionRepository.findTop10ByOrderBySubmittedAtDesc()
                .stream()
                .map(s -> {
                    Problem problem = problemRepository.findById(s.getProblemId()).orElse(null);
                    return buildResponse(s, problem, new ArrayList<>());
                })
                .toList();
    }

    private SubmissionResponse buildResponse(Submission submission, Problem problem, 
                                              List<SubmissionResponse.TestCaseResult> testCaseResults) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .problemId(submission.getProblemId())
                .problemTitle(problem != null ? problem.getTitle() : "Unknown")
                .languageId(submission.getLanguageId())
                .languageName(submission.getLanguageName())
                .verdict(submission.getVerdict())
                .executionTime(submission.getExecutionTime())
                .memoryUsed(submission.getMemoryUsed())
                .output(submission.getOutput())
                .compileOutput(submission.getCompileOutput())
                .errorMessage(submission.getErrorMessage())
                .submittedAt(submission.getSubmittedAt())
                .testCaseResults(testCaseResults)
                .build();
    }

    private String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }
        return output.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
    }
}
