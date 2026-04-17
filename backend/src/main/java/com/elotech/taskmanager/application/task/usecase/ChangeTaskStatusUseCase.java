package com.elotech.taskmanager.application.task.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.task.TaskStatus;
import com.elotech.taskmanager.domain.user.Role;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ChangeTaskStatusUseCase {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ChangeTaskStatusUseCase(TaskRepository taskRepository,
                                   ProjectRepository projectRepository,
                                   UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Task execute(Long taskId, TaskStatus newStatus, Long projectId, Long requesterId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DomainException("Projeto nao encontrado"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new DomainException("Usuario nao encontrado"));

        if (!project.isMember(requester)) {
            throw new DomainException("Usuario nao pertence a este projeto");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Tarefa nao encontrada"));

        // Regra 3: WIP limit — max 5 IN_PROGRESS por responsavel
        if (newStatus == TaskStatus.IN_PROGRESS && task.getAssignee() != null) {
            long inProgressCount = taskRepository
                    .findAllByAssigneeIdAndStatus(task.getAssignee().getId(), TaskStatus.IN_PROGRESS)
                    .size();
            if (inProgressCount >= 5) {
                throw new DomainException("Responsavel ja possui 5 tarefas IN_PROGRESS (limite WIP atingido)");
            }
        }

        // Regras 1 e 2 estao dentro da entidade Task.changeStatus()
        Role userRole = project.getRoleOf(requester);
        task.changeStatus(newStatus, userRole);

        return taskRepository.save(task);
    }
}
