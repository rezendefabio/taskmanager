package com.elotech.taskmanager.application.project.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUserProjectsUseCase {
    private final ProjectRepository projectRepository;

    public ListUserProjectsUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> execute(Long userId) {
        return projectRepository.findAllByMemberId(userId);
    }
}
