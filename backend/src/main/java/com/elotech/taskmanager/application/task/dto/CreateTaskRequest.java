package com.elotech.taskmanager.application.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        @NotNull String priority,
        LocalDateTime deadline,
        Long assigneeId
) {}
