package com.onlinejudge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Local code executor that runs code directly on the machine.
 * Supports Python, Java, JavaScript, C, and C++.
 * 
 * Enabled when: executor.mode=local (default)
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "executor.mode", havingValue = "local", matchIfMissing = true)
public class LocalCodeExecutor implements CodeExecutor {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/onlinejudge";

    // Language ID to configuration mapping
    private static final Map<Integer, LanguageConfig> LANGUAGES = Map.of(
            71, new LanguageConfig("python", "py", null, "python3 {file}"),
            62, new LanguageConfig("java", "java", "javac {file}", "java -cp {dir} Main"),
            54, new LanguageConfig("cpp", "cpp", "g++ -o {dir}/a.out {file}", "{dir}/a.out"),
            63, new LanguageConfig("javascript", "js", null, "node {file}"),
            50, new LanguageConfig("c", "c", "gcc -o {dir}/a.out {file}", "{dir}/a.out")
    );

    @Override
    public String getExecutorType() {
        return "LOCAL";
    }

    @Override
    public ExecutionResult execute(String sourceCode, int languageId, String stdin, 
                                    int timeLimitMs, int memoryLimitKb) {
        LanguageConfig config = LANGUAGES.get(languageId);
        if (config == null) {
            return ExecutionResult.error("Unsupported language ID: " + languageId);
        }

        String executionId = UUID.randomUUID().toString().substring(0, 8);
        Path workDir = Paths.get(TEMP_DIR, executionId);
        
        try {
            // Create work directory
            Files.createDirectories(workDir);
            
            // Write source code to file
            String filename = config.language.equals("java") ? "Main." + config.extension : "solution." + config.extension;
            Path sourceFile = workDir.resolve(filename);
            Files.writeString(sourceFile, sourceCode);
            
            long startTime = System.currentTimeMillis();
            
            // Compile if needed
            if (config.compileCommand != null) {
                ExecutionResult compileResult = runProcess(
                        config.compileCommand
                                .replace("{file}", sourceFile.toString())
                                .replace("{dir}", workDir.toString()),
                        workDir, null, 30000 // 30 second compile timeout
                );
                
                if (compileResult.exitCode != 0) {
                    return ExecutionResult.compilationError(compileResult.stderr);
                }
            }
            
            // Execute
            String runCommand = config.runCommand
                    .replace("{file}", sourceFile.toString())
                    .replace("{dir}", workDir.toString());
            
            ExecutionResult result = runProcess(runCommand, workDir, stdin, timeLimitMs);
            result.executionTimeMs = System.currentTimeMillis() - startTime;
            
            return result;
            
        } catch (Exception e) {
            log.error("Execution failed", e);
            return ExecutionResult.error("Execution failed: " + e.getMessage());
        } finally {
            // Cleanup
            try {
                deleteDirectory(workDir);
            } catch (IOException e) {
                log.warn("Failed to cleanup temp directory: {}", workDir);
            }
        }
    }

    private ExecutionResult runProcess(String command, Path workDir, String stdin, int timeoutMs) {
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(false);
            
            Process process = pb.start();
            
            // Write stdin if provided
            if (stdin != null && !stdin.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(stdin.getBytes());
                    os.flush();
                }
            } else {
                process.getOutputStream().close();
            }
            
            // Read stdout and stderr with timeout
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> stdoutFuture = executor.submit(() -> readStream(process.getInputStream()));
            Future<String> stderrFuture = executor.submit(() -> readStream(process.getErrorStream()));
            
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                executor.shutdownNow();
                return ExecutionResult.timeLimitExceeded();
            }
            
            String stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
            String stderr = stderrFuture.get(1, TimeUnit.SECONDS);
            executor.shutdown();
            
            int exitCode = process.exitValue();
            
            if (exitCode != 0 && (stderr.contains("Exception") || stderr.contains("Error") || 
                    stderr.contains("Segmentation fault") || stderr.contains("core dumped"))) {
                return ExecutionResult.runtimeError(stderr, exitCode);
            }
            
            return new ExecutionResult(stdout, stderr, exitCode, 0);
            
        } catch (TimeoutException e) {
            return ExecutionResult.timeLimitExceeded();
        } catch (Exception e) {
            return ExecutionResult.error("Process execution failed: " + e.getMessage());
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete: {}", p);
                        }
                    });
        }
    }

    private record LanguageConfig(String language, String extension, String compileCommand, String runCommand) {}
}
