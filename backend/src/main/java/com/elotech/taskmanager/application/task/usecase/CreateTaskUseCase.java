package com.elotech.taskmanager.application.task.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.shared.NotFoundException;
import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.task.TaskPriority;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreateTaskUseCase {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public CreateTaskUseCase(TaskRepository taskRepository,
                             ProjectRepository projectRepository,
                             UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Task execute(Long projectId, String title, String description,
                        TaskPriority priority, LocalDateTime deadline,
                        Long assigneeId, Long requesterId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Projeto nao encontrado"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        if (!project.isMember(requester)) {
            throw new DomainException("Usuario nao pertence a este projeto");
        }

        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("Responsavel nao encontrado"));

            if (!project.isMember(assignee)) {
                throw new DomainException("Responsavel nao pertence a este projeto");
            }
        }

        Task task = Task.create(title, description, priority, deadline, assignee, project);

        return taskRepository.save(task);
    }
}
