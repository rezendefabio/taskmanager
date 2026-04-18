package com.elotech.taskmanager.application.task.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.shared.NotFoundException;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteTaskUseCase {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public DeleteTaskUseCase(TaskRepository taskRepository,
                             ProjectRepository projectRepository,
                             UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public void execute(Long taskId, Long projectId, Long requesterId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Projeto nao encontrado"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        if (!project.isMember(requester)) {
            throw new DomainException("Usuario nao pertence a este projeto");
        }

        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Tarefa nao encontrada");
        }

        taskRepository.deleteById(taskId);
    }
}
