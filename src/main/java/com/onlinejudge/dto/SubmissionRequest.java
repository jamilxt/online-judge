package com.onlinejudge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {
    
    @NotNull(message = "Problem ID is required")
    private Long problemId;
    
    @NotNull(message = "Language ID is required")
    private Integer languageId;
    
    @NotBlank(message = "Source code is required")
    private String sourceCode;
}
