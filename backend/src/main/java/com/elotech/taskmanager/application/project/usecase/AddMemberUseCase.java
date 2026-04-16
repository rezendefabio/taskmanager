package com.elotech.taskmanager.application.project.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AddMemberUseCase {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public AddMemberUseCase(ProjectRepository projectRepository,
                            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project execute(Long projectId, Long userId, Long requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DomainException("Projeto nao encontrado"));

        if (!project.getOwner().getId().equals(requesterId)) {
            throw new DomainException("Apenas o dono do projeto pode adicionar membros");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario nao encontrado"));

        project.addMember(user);

        return projectRepository.save(project);
    }
}
