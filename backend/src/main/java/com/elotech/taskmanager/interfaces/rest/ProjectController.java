package com.elotech.taskmanager.interfaces.rest;

import com.elotech.taskmanager.application.project.dto.CreateProjectRequest;
import com.elotech.taskmanager.application.project.dto.ProjectResponse;
import com.elotech.taskmanager.application.project.usecase.AddMemberUseCase;
import com.elotech.taskmanager.application.project.usecase.CreateProjectUseCase;
import com.elotech.taskmanager.application.project.usecase.ListUserProjectsUseCase;
import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import com.elotech.taskmanager.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final CreateProjectUseCase createProjectUseCase;
    private final AddMemberUseCase addMemberUseCase;
    private final ListUserProjectsUseCase listUserProjectsUseCase;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public ProjectController(CreateProjectUseCase createProjectUseCase,
                             AddMemberUseCase addMemberUseCase,
                             ListUserProjectsUseCase listUserProjectsUseCase,
                             ProjectRepository projectRepository,
                             TaskRepository taskRepository,
                             UserRepository userRepository,
                             JwtService jwtService) {
        this.createProjectUseCase = createProjectUseCase;
        this.addMemberUseCase = addMemberUseCase;
        this.listUserProjectsUseCase = listUserProjectsUseCase;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request,
                                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        Project project = createProjectUseCase.execute(request.name(), request.description(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectResponse.fromEntity(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        List<ProjectResponse> projects = listUserProjectsUseCase.execute(userId).stream()
                .map(ProjectResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long projectId,
                                                   @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DomainException("Projeto nao encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario nao encontrado"));

        if (!project.isMember(user)) {
            throw new DomainException("Usuario nao pertence a este projeto");
        }

        return ResponseEntity.ok(ProjectResponse.fromEntity(project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long projectId,
                                                  @Valid @RequestBody CreateProjectRequest request,
                                                  @RequestHeader("Authorization") String authHeader) {
        Long requesterId = extractUserId(authHeader);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DomainException("Projeto nao encontrado"));

        if (!project.getOwner().getId().equals(requesterId)) {
            throw new DomainException("Apenas o dono pode editar o projeto");
        }

        project.update(request.name(), request.description());
        project = projectRepository.save(project);
        return ResponseEntity.ok(ProjectResponse.fromEntity(project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @RequestHeader("Authorization") String authHeader) {
        Long requesterId = extractUserId(authHeader);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DomainException("Projeto nao encontrado"));

        if (!project.getOwner().getId().equals(requesterId)) {
            throw new DomainException("Apenas o dono pode excluir o projeto");
        }

        taskRepository.deleteAll(taskRepository.findAllByProjectId(projectId));
        projectRepository.delete(project);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectResponse> addMember(@PathVariable Long projectId,
                                                     @PathVariable Long userId,
                                                     @RequestHeader("Authorization") String authHeader) {
        Long requesterId = extractUserId(authHeader);
        Project project = addMemberUseCase.execute(projectId, userId, requesterId);
        return ResponseEntity.ok(ProjectResponse.fromEntity(project));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectResponse> removeMember(@PathVariable Long projectId,
                                                        @PathVariable Long userId,
                                                        @RequestHeader("Authorization") String authHeader) {
        Long requesterId = extractUserId(authHeader);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DomainException("Projeto nao encontrado"));

        if (!project.getOwner().getId().equals(requesterId)) {
            throw new DomainException("Apenas o dono pode remover membros");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("Usuario nao encontrado"));

        project.removeMember(user);
        project = projectRepository.save(project);
        return ResponseEntity.ok(ProjectResponse.fromEntity(project));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractClaims(token).get("userId", Long.class);
    }
}
