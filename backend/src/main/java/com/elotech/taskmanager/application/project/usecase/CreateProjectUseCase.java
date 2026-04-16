package com.elotech.taskmanager.application.project.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateProjectUseCase {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public CreateProjectUseCase(ProjectRepository projectRepository,
                                UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project execute(String name, String description, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new DomainException("Usuario nao encontrado"));

        Project project = Project.create(name, description, owner);

        return projectRepository.save(project);
    }
}
