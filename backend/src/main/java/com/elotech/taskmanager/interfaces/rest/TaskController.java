package com.elotech.taskmanager.interfaces.rest;

import com.elotech.taskmanager.application.task.dto.CreateTaskRequest;
import com.elotech.taskmanager.application.task.dto.TaskResponse;
import com.elotech.taskmanager.application.task.dto.UpdateTaskRequest;
import com.elotech.taskmanager.application.task.usecase.ChangeTaskStatusUseCase;
import com.elotech.taskmanager.application.task.usecase.CreateTaskUseCase;
import com.elotech.taskmanager.application.task.usecase.DeleteTaskUseCase;
import com.elotech.taskmanager.application.task.usecase.UpdateTaskUseCase;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.task.TaskPriority;
import com.elotech.taskmanager.domain.task.TaskStatus;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final ChangeTaskStatusUseCase changeTaskStatusUseCase;
    private final TaskRepository taskRepository;
    private final JwtService jwtService;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          UpdateTaskUseCase updateTaskUseCase,
                          DeleteTaskUseCase deleteTaskUseCase,
                          ChangeTaskStatusUseCase changeTaskStatusUseCase,
                          TaskRepository taskRepository,
                          JwtService jwtService) {
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.changeTaskStatusUseCase = changeTaskStatusUseCase;
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

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long taskId,
                                       @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        deleteTaskUseCase.execute(taskId, projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listByProject(@PathVariable Long projectId) {
        List<TaskResponse> tasks = taskRepository.findAll().stream()
                .map(TaskResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long projectId,
                                                @PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException("Tarefa nao encontrada"));
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
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
