package com.elotech.taskmanager.application.task.usecase;

import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.task.TaskPriority;
import com.elotech.taskmanager.domain.task.TaskStatus;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ListTasksUseCase {

    private final TaskRepository taskRepository;

    public ListTasksUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<Task> execute(TaskStatus status, TaskPriority priority,
                              Long assigneeId, LocalDateTime from,
                              LocalDateTime to, String search,
                              Pageable pageable) {

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.hasAssignee(assigneeId))
                .and(TaskSpecification.createdAfter(from))
                .and(TaskSpecification.createdBefore(to))
                .and(TaskSpecification.titleOrDescriptionContains(search));

        return taskRepository.findAll(spec, pageable);
    }
}
