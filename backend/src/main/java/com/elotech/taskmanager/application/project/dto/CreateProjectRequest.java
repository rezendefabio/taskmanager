package com.elotech.taskmanager.application.project.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank String name,
        String description
) {}
