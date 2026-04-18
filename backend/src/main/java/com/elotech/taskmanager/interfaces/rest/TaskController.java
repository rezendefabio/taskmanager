package com.elotech.taskmanager.interfaces.rest;

import com.elotech.taskmanager.application.project.usecase.GetProjectReportUseCase;
import com.elotech.taskmanager.application.task.dto.CreateTaskRequest;
import com.elotech.taskmanager.application.task.dto.TaskResponse;
import com.elotech.taskmanager.application.task.dto.UpdateTaskRequest;
import com.elotech.taskmanager.application.task.usecase.AssignTaskUseCase;
import com.elotech.taskmanager.application.task.usecase.ChangeTaskStatusUseCase;
import com.elotech.taskmanager.application.task.usecase.CreateTaskUseCase;
import com.elotech.taskmanager.application.task.usecase.DeleteTaskUseCase;
import com.elotech.taskmanager.application.task.usecase.ListTasksUseCase;
import com.elotech.taskmanager.application.task.usecase.UpdateTaskUseCase;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.task.TaskPriority;
import com.elotech.taskmanager.domain.task.TaskStatus;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final ChangeTaskStatusUseCase changeTaskStatusUseCase;
    private final AssignTaskUseCase assignTaskUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final GetProjectReportUseCase getProjectReportUseCase;
    private final TaskRepository taskRepository;
    private final JwtService jwtService;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          UpdateTaskUseCase updateTaskUseCase,
                          DeleteTaskUseCase deleteTaskUseCase,
                          ChangeTaskStatusUseCase changeTaskStatusUseCase,
                          AssignTaskUseCase assignTaskUseCase,
                          ListTasksUseCase listTasksUseCase,
                          GetProjectReportUseCase getProjectReportUseCase,
                          TaskRepository taskRepository,
                          JwtService jwtService) {
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.changeTaskStatusUseCase = changeTaskStatusUseCase;
        this.assignTaskUseCase = assignTaskUseCase;
        this.listTasksUseCase = listTasksUseCase;
        this.getProjectReportUseCase = getProjectReportUseCase;
        this.taskRepository = taskRepository;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@PathVariable Long projectId,
                                               @Valid @RequestBody CreateTaskRequest request,
                                               @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        TaskPriority priority = parsePriority(request.priority());

        Task task = createTaskUseCase.execute(
                projectId, request.title(), request.description(),
                priority, request.deadline(), request.assigneeId(), userId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.fromEntity(task));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long projectId,
                                               @PathVariable Long taskId,
                                               @Valid @RequestBody UpdateTaskRequest request,
                                               @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);

        Task task = updateTaskUseCase.execute(
                taskId, projectId, request.title(), request.description(),
                request.deadline(), userId
        );

        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> changeStatus(@PathVariable Long projectId,
                                                     @PathVariable Long taskId,
                                                     @RequestBody String newStatus,
                                                     @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        TaskStatus status = parseStatus(newStatus.replace("\"", "").trim());

        Task task = changeTaskStatusUseCase.execute(taskId, status, projectId, userId);

        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<TaskResponse> changePriority(@PathVariable Long projectId,
                                                       @PathVariable Long taskId,
                                                       @RequestBody String newPriority,
                                                       @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        TaskPriority priority = parsePriority(newPriority.replace("\"", "").trim());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Tarefa nao encontrada"));

        task.changePriority(priority);
        task = taskRepository.save(task);

        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @PatchMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> assign(@PathVariable Long projectId,
                                               @PathVariable Long taskId,
                                               @RequestBody Map<String, Long> body,
                                               @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        Long assigneeId = body.get("assigneeId");
        Task task = assignTaskUseCase.execute(taskId, assigneeId, projectId, userId);
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long taskId,
                                       @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        deleteTaskUseCase.execute(taskId, projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> listByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        TaskStatus taskStatus = status != null ? parseStatus(status) : null;
        TaskPriority taskPriority = priority != null ? parsePriority(priority) : null;

        Page<TaskResponse> tasks = listTasksUseCase
                .execute(projectId, taskStatus, taskPriority, assigneeId, from, to, search, pageable)
                .map(TaskResponse::fromEntity);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long projectId,
                                                @PathVariable Long taskId,
                                                @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Tarefa nao encontrada"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new DomainException("Tarefa nao pertence a este projeto");
        }

        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @GetMapping("/report")
    public ResponseEntity<Map<String, Map<String, Long>>> report(@PathVariable Long projectId) {
        return ResponseEntity.ok(getProjectReportUseCase.execute(projectId));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractClaims(token).get("userId", Long.class);
    }

    private TaskPriority parsePriority(String priority) {
        try {
            return TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Prioridade invalida: " + priority + ". Use: LOW, MEDIUM, HIGH, CRITICAL");
        }
    }

    private TaskStatus parseStatus(String status) {
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Status invalido: " + status + ". Use: TODO, IN_PROGRESS, DONE");
        }
    }
}
