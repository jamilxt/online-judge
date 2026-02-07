package com.onlinejudge.config;

import com.onlinejudge.model.Problem;
import com.onlinejudge.model.TestCase;
import com.onlinejudge.repository.ProblemRepository;
import com.onlinejudge.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing sample problems...");

        // Problem 1: Two Sum
        Problem twoSum = problemRepository.save(Problem.builder()
                .title("Two Sum")
                .description("""
                    ## Problem Statement
                    Given two integers, return their sum.
                    
                    ## Input Format
                    Two space-separated integers `a` and `b` where `-1000 ≤ a, b ≤ 1000`
                    
                    ## Output Format
                    A single integer representing the sum of `a` and `b`.
                    
                    ## Example
                    **Input:**
                    ```
                    3 5
                    ```
                    **Output:**
                    ```
                    8
                    ```
                    """)
                .difficulty(Problem.Difficulty.EASY)
                .timeLimit(1000)
                .memoryLimit(128000)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(twoSum.getId())
                .input("3 5")
                .expectedOutput("8")
                .isHidden(false)
                .orderIndex(0)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(twoSum.getId())
                .input("-10 20")
                .expectedOutput("10")
                .isHidden(false)
                .orderIndex(1)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(twoSum.getId())
                .input("0 0")
                .expectedOutput("0")
                .isHidden(true)
                .orderIndex(2)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(twoSum.getId())
                .input("-500 500")
                .expectedOutput("0")
                .isHidden(true)
                .orderIndex(3)
                .build());

        // Problem 2: Palindrome Check
        Problem palindrome = problemRepository.save(Problem.builder()
                .title("Palindrome Check")
                .description("""
                    ## Problem Statement
                    Given a string, determine if it is a palindrome. A palindrome reads the same forwards and backwards.
                    
                    ## Input Format
                    A single line containing a string `s` (1 ≤ |s| ≤ 100) consisting of lowercase English letters.
                    
                    ## Output Format
                    Print `YES` if the string is a palindrome, otherwise print `NO`.
                    
                    ## Example
                    **Input:**
                    ```
                    racecar
                    ```
                    **Output:**
                    ```
                    YES
                    ```
                    """)
                .difficulty(Problem.Difficulty.EASY)
                .timeLimit(1000)
                .memoryLimit(128000)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(palindrome.getId())
                .input("racecar")
                .expectedOutput("YES")
                .isHidden(false)
                .orderIndex(0)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(palindrome.getId())
                .input("hello")
                .expectedOutput("NO")
                .isHidden(false)
                .orderIndex(1)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(palindrome.getId())
                .input("a")
                .expectedOutput("YES")
                .isHidden(true)
                .orderIndex(2)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(palindrome.getId())
                .input("abba")
                .expectedOutput("YES")
                .isHidden(true)
                .orderIndex(3)
                .build());

        // Problem 3: FizzBuzz
        Problem fizzBuzz = problemRepository.save(Problem.builder()
                .title("FizzBuzz")
                .description("""
                    ## Problem Statement
                    Given an integer `n`, print: 
                    - `FizzBuzz` if `n` is divisible by both 3 and 5
                    - `Fizz` if `n` is divisible by 3
                    - `Buzz` if `n` is divisible by 5
                    - The number itself otherwise
                    
                    ## Input Format
                    A single integer `n` (1 ≤ n ≤ 1000)
                    
                    ## Output Format
                    A single line containing the result.
                    
                    ## Example
                    **Input:**
                    ```
                    15
                    ```
                    **Output:**
                    ```
                    FizzBuzz
                    ```
                    """)
                .difficulty(Problem.Difficulty.EASY)
                .timeLimit(1000)
                .memoryLimit(128000)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(fizzBuzz.getId())
                .input("15")
                .expectedOutput("FizzBuzz")
                .isHidden(false)
                .orderIndex(0)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(fizzBuzz.getId())
                .input("9")
                .expectedOutput("Fizz")
                .isHidden(false)
                .orderIndex(1)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(fizzBuzz.getId())
                .input("10")
                .expectedOutput("Buzz")
                .isHidden(false)
                .orderIndex(2)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(fizzBuzz.getId())
                .input("7")
                .expectedOutput("7")
                .isHidden(true)
                .orderIndex(3)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(fizzBuzz.getId())
                .input("30")
                .expectedOutput("FizzBuzz")
                .isHidden(true)
                .orderIndex(4)
                .build());

        // Problem 4: Factorial
        Problem factorial = problemRepository.save(Problem.builder()
                .title("Factorial")
                .description("""
                    ## Problem Statement
                    Calculate the factorial of a given non-negative integer `n`.
                    
                    The factorial of `n` (denoted as `n!`) is the product of all positive integers less than or equal to `n`.
                    
                    ## Input Format
                    A single non-negative integer `n` (0 ≤ n ≤ 12)
                    
                    ## Output Format
                    A single integer representing `n!`
                    
                    ## Example
                    **Input:**
                    ```
                    5
                    ```
                    **Output:**
                    ```
                    120
                    ```
                    
                    ## Note
                    - 0! = 1 by definition
                    - 5! = 5 × 4 × 3 × 2 × 1 = 120
                    """)
                .difficulty(Problem.Difficulty.MEDIUM)
                .timeLimit(1000)
                .memoryLimit(128000)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(factorial.getId())
                .input("5")
                .expectedOutput("120")
                .isHidden(false)
                .orderIndex(0)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(factorial.getId())
                .input("0")
                .expectedOutput("1")
                .isHidden(false)
                .orderIndex(1)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(factorial.getId())
                .input("10")
                .expectedOutput("3628800")
                .isHidden(true)
                .orderIndex(2)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(factorial.getId())
                .input("12")
                .expectedOutput("479001600")
                .isHidden(true)
                .orderIndex(3)
                .build());

        // Problem 5: Prime Check
        Problem primeCheck = problemRepository.save(Problem.builder()
                .title("Prime Number Check")
                .description("""
                    ## Problem Statement
                    Given a positive integer `n`, determine if it is a prime number.
                    
                    A prime number is a natural number greater than 1 that has no positive divisors other than 1 and itself.
                    
                    ## Input Format
                    A single positive integer `n` (1 ≤ n ≤ 10^6)
                    
                    ## Output Format
                    Print `YES` if `n` is prime, otherwise print `NO`.
                    
                    ## Example
                    **Input:**
                    ```
                    17
                    ```
                    **Output:**
                    ```
                    YES
                    ```
                    """)
                .difficulty(Problem.Difficulty.MEDIUM)
                .timeLimit(2000)
                .memoryLimit(128000)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(primeCheck.getId())
                .input("17")
                .expectedOutput("YES")
                .isHidden(false)
                .orderIndex(0)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(primeCheck.getId())
                .input("1")
                .expectedOutput("NO")
                .isHidden(false)
                .orderIndex(1)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(primeCheck.getId())
                .input("2")
                .expectedOutput("YES")
                .isHidden(false)
                .orderIndex(2)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(primeCheck.getId())
                .input("999983")
                .expectedOutput("YES")
                .isHidden(true)
                .orderIndex(3)
                .build());

        testCaseRepository.save(TestCase.builder()
                .problemId(primeCheck.getId())
                .input("100")
                .expectedOutput("NO")
                .isHidden(true)
                .orderIndex(4)
                .build());

        log.info("Initialized {} sample problems", problemRepository.count());
    }
}
