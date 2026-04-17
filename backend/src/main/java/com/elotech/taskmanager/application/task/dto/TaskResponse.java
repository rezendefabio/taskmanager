package com.elotech.taskmanager.application.task.dto;

import com.elotech.taskmanager.domain.task.Task;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deadline,
        String assigneeName
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDeadline(),
                task.getAssignee() != null ? task.getAssignee().getName() : null
        );
    }
}
