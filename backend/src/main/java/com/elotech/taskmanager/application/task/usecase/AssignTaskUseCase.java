package com.elotech.taskmanager.application.task.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.shared.NotFoundException;
import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AssignTaskUseCase {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public AssignTaskUseCase(TaskRepository taskRepository,
                             ProjectRepository projectRepository,
                             UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Task execute(Long taskId, Long assigneeId, Long projectId, Long requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Projeto nao encontrado"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        if (!project.isMember(requester)) {
            throw new DomainException("Usuario nao pertence a este projeto");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Tarefa nao encontrada"));

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("Responsavel nao encontrado"));
            if (!project.isMember(assignee)) {
                throw new DomainException("Responsavel nao pertence a este projeto");
            }
            task.assignTo(assignee);
        } else {
            task.assignTo(null);
        }

        return taskRepository.save(task);
    }
}
