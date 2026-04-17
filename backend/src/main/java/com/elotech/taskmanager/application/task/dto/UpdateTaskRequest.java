package com.elotech.taskmanager.application.task.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record UpdateTaskRequest(
        @NotBlank String title,
        String description,
        LocalDateTime deadline
) {}
