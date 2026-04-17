package com.elotech.taskmanager.application.project.usecase;

import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GetProjectReportUseCase {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public GetProjectReportUseCase(TaskRepository taskRepository,
                                   ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public Map<String, Map<String, Long>> execute(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new DomainException("Projeto nao encontrado");
        }

        var tasks = taskRepository.findAllByProjectId(projectId);

        Map<String, Long> byStatus = new HashMap<>();
        Map<String, Long> byPriority = new HashMap<>();

        for (var task : tasks) {
            String status = task.getStatus().name();
            byStatus.merge(status, 1L, Long::sum);

            String priority = task.getPriority().name();
            byPriority.merge(priority, 1L, Long::sum);
        }

        Map<String, Map<String, Long>> report = new HashMap<>();
        report.put("byStatus", byStatus);
        report.put("byPriority", byPriority);

        return report;
    }
}
