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
 * Docker-based code executor that runs code in isolated containers.
 * Provides better security than local execution.
 * 
 * Enabled when: executor.mode=docker
 * 
 * Required Docker images (will be pulled automatically):
 * - python:3.9-slim
 * - openjdk:17-slim
 * - gcc:latest
 * - node:18-slim
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "executor.mode", havingValue = "docker")
public class DockerCodeExecutor implements CodeExecutor {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/onlinejudge-docker";

    // Language configuration for Docker execution
    private static final Map<Integer, DockerLanguageConfig> LANGUAGES = Map.of(
            71, new DockerLanguageConfig("python", "py", "python:3.9-slim", null, "python3 /code/solution.py"),
            62, new DockerLanguageConfig("java", "java", "openjdk:17-slim", "javac /code/Main.java", "java -cp /code Main"),
            54, new DockerLanguageConfig("cpp", "cpp", "gcc:latest", "g++ -o /code/a.out /code/solution.cpp", "/code/a.out"),
            63, new DockerLanguageConfig("javascript", "js", "node:18-slim", null, "node /code/solution.js"),
            50, new DockerLanguageConfig("c", "c", "gcc:latest", "gcc -o /code/a.out /code/solution.c", "/code/a.out")
    );

    @Override
    public String getExecutorType() {
        return "DOCKER";
    }

    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "info");
            Process process = pb.start();
            boolean completed = process.waitFor(5, TimeUnit.SECONDS);
            return completed && process.exitValue() == 0;
        } catch (Exception e) {
            log.warn("Docker is not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ExecutionResult execute(String sourceCode, int languageId, String stdin, 
                                    int timeLimitMs, int memoryLimitKb) {
        DockerLanguageConfig config = LANGUAGES.get(languageId);
        if (config == null) {
            return ExecutionResult.error("Unsupported language ID: " + languageId);
        }

        if (!isAvailable()) {
            return ExecutionResult.error("Docker is not available. Please install Docker or switch to local execution mode.");
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

            // Write stdin to file if provided
            Path stdinFile = workDir.resolve("stdin.txt");
            Files.writeString(stdinFile, stdin != null ? stdin : "");
            
            long startTime = System.currentTimeMillis();
            
            // Compile if needed (inside container)
            if (config.compileCommand != null) {
                ExecutionResult compileResult = runDocker(
                        config.image, 
                        workDir, 
                        config.compileCommand,
                        null,
                        30000, // 30 second compile timeout
                        memoryLimitKb
                );
                
                if (compileResult.exitCode != 0) {
                    return ExecutionResult.compilationError(compileResult.stderr);
                }
            }
            
            // Execute
            ExecutionResult result = runDocker(
                    config.image,
                    workDir,
                    config.runCommand,
                    stdinFile,
                    timeLimitMs,
                    memoryLimitKb
            );
            result.executionTimeMs = System.currentTimeMillis() - startTime;
            
            return result;
            
        } catch (Exception e) {
            log.error("Docker execution failed", e);
            return ExecutionResult.error("Docker execution failed: " + e.getMessage());
        } finally {
            // Cleanup
            try {
                deleteDirectory(workDir);
            } catch (IOException e) {
                log.warn("Failed to cleanup temp directory: {}", workDir);
            }
        }
    }

    private ExecutionResult runDocker(String image, Path workDir, String command, 
                                       Path stdinFile, int timeoutMs, int memoryLimitKb) {
        try {
            // Calculate memory limit in MB (minimum 32MB)
            int memoryMb = Math.max(32, memoryLimitKb / 1024);
            
            // Build docker command
            // --rm: Remove container after execution
            // --network none: No network access
            // --memory: Memory limit
            // --cpus: CPU limit
            // -v: Mount code directory
            ProcessBuilder pb;
            
            if (stdinFile != null && Files.exists(stdinFile)) {
                // With stdin
                pb = new ProcessBuilder(
                        "bash", "-c",
                        String.format(
                                "cat %s | docker run --rm --network none --memory=%dm --cpus=0.5 " +
                                "-v %s:/code:rw -i %s sh -c '%s'",
                                stdinFile.toString(),
                                memoryMb,
                                workDir.toAbsolutePath(),
                                image,
                                command
                        )
                );
            } else {
                // Without stdin
                pb = new ProcessBuilder(
                        "docker", "run", "--rm", "--network", "none",
                        "--memory=" + memoryMb + "m",
                        "--cpus=0.5",
                        "-v", workDir.toAbsolutePath() + ":/code:rw",
                        image,
                        "sh", "-c", command
                );
            }
            
            pb.redirectErrorStream(false);
            Process process = pb.start();
            
            // Close stdin immediately if not providing input via file
            process.getOutputStream().close();
            
            // Read stdout and stderr with timeout
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> stdoutFuture = executor.submit(() -> readStream(process.getInputStream()));
            Future<String> stderrFuture = executor.submit(() -> readStream(process.getErrorStream()));
            
            boolean completed = process.waitFor(timeoutMs + 2000, TimeUnit.MILLISECONDS); // Extra time for Docker overhead
            
            if (!completed) {
                process.destroyForcibly();
                executor.shutdownNow();
                // Also try to stop any running container
                stopContainer(workDir.getFileName().toString());
                return ExecutionResult.timeLimitExceeded();
            }
            
            String stdout = stdoutFuture.get(2, TimeUnit.SECONDS);
            String stderr = stderrFuture.get(2, TimeUnit.SECONDS);
            executor.shutdown();
            
            int exitCode = process.exitValue();
            
            // Check for OOM killed
            if (stderr.contains("Killed") || exitCode == 137) {
                ExecutionResult r = new ExecutionResult("", "Memory limit exceeded", 137, 0);
                r.status = ExecutionResult.ResultStatus.MEMORY_LIMIT_EXCEEDED;
                return r;
            }
            
            if (exitCode != 0 && !stderr.isEmpty()) {
                return ExecutionResult.runtimeError(stderr, exitCode);
            }
            
            return new ExecutionResult(stdout, stderr, exitCode, 0);
            
        } catch (TimeoutException e) {
            return ExecutionResult.timeLimitExceeded();
        } catch (Exception e) {
            log.error("Docker process failed", e);
            return ExecutionResult.error("Docker process failed: " + e.getMessage());
        }
    }

    private void stopContainer(String containerId) {
        try {
            new ProcessBuilder("docker", "stop", "-t", "1", containerId).start();
        } catch (Exception e) {
            // Ignore - container might already be stopped
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

    private record DockerLanguageConfig(String language, String extension, String image, 
                                         String compileCommand, String runCommand) {}
}
