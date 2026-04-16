package com.elotech.taskmanager.domain.task;

import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.user.Role;
import com.elotech.taskmanager.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    protected Task() {}

    private Task(String title, String description, TaskPriority priority, LocalDateTime deadline, User assignee) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;
        this.priority = priority;
        this.deadline = deadline;
        this.assignee = assignee;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Task create(String title, String description, TaskPriority priority, LocalDateTime deadline, User assignee) {
        if (title == null || title.isBlank()) {
            throw new DomainException("Título é obrigatório");
        }

        if (priority == null) {
            throw new DomainException("Prioridade é obrigatória");
        }

        return new Task(title.trim(), description, priority, deadline, assignee);
    }

    //REGRA 1: DONE não pode voltar para TODO
    //REGRA 2: Tarefa CRITICAL só pode ser fechada por ADMIN
    public void changeStatus(TaskStatus newStatus, Role userRole) {
        if (this.status == TaskStatus.DONE && newStatus == TaskStatus.TODO) {
            throw new DomainException("Tarefa DONE não pode voltar para TODO");
        }
        if (this.priority == TaskPriority.CRITICAL &&
                newStatus == TaskStatus.DONE &&
                userRole != Role.ADMIN) {
            throw new DomainException("Apenas ADMIN pode fechar tarefa CRITICAL");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignTo(User user) {
        this.assignee = user;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePriority(TaskPriority newPriority) {
        if (this.status == TaskStatus.DONE) {
            throw new DomainException("Não é permitido alterar prioridade de tarefa DONE");
        }
        this.priority = newPriority;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String description, LocalDateTime deadline) {
        if (title == null || title.isBlank()) {
            throw new DomainException("Título é obrigatório");
        }
        this.title = title.trim();
        this.description = description;
        this.deadline = deadline;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public TaskPriority getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public User getAssignee() { return assignee; }
}
