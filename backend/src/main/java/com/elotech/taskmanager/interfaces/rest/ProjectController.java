package com.elotech.taskmanager.interfaces.rest;

import com.elotech.taskmanager.application.project.dto.CreateProjectRequest;
import com.elotech.taskmanager.application.project.dto.ProjectResponse;
import com.elotech.taskmanager.application.project.usecase.AddMemberUseCase;
import com.elotech.taskmanager.application.project.usecase.CreateProjectUseCase;
import com.elotech.taskmanager.application.project.usecase.ListUserProjectsUseCase;
import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController{
    private final CreateProjectUseCase createProjectUseCase;
    private final AddMemberUseCase addMemberUseCase;
    private final ListUserProjectsUseCase listUserProjectsUseCase;
    private final JwtService jwtService;

    public ProjectController(CreateProjectUseCase createProjectUseCase,
                             AddMemberUseCase addMemberUseCase,
                             ListUserProjectsUseCase listUserProjectsUseCase,
                             JwtService jwtService) {
        this.createProjectUseCase = createProjectUseCase;
        this.addMemberUseCase = addMemberUseCase;
        this.listUserProjectsUseCase = listUserProjectsUseCase;
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

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectResponse> addMember(@PathVariable Long projectId,
                                                     @PathVariable Long userId,
                                                     @RequestHeader("Authorization") String authHeader) {
        Long requesterId = extractUserId(authHeader);
        Project project = addMemberUseCase.execute(projectId, userId, requesterId);
        return ResponseEntity.ok(ProjectResponse.fromEntity(project));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractClaims(token).get("userId", Long.class);
    }
}
