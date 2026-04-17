package com.elotech.taskmanager.application.task.usecase;

import com.elotech.taskmanager.domain.project.Project;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.task.TaskPriority;
import com.elotech.taskmanager.domain.task.TaskStatus;
import com.elotech.taskmanager.domain.user.Role;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.ProjectRepository;
import com.elotech.taskmanager.infrastructure.persistence.TaskRepository;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeTaskStatusUseCaseTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChangeTaskStatusUseCase useCase;

    private User admin;
    private User member;
    private Project project;

    @BeforeEach
    void setUp() {
        admin = User.create("Admin", "admin@test.com", "123456", Role.ADMIN);
        admin.setId(1L);

        member = User.create("Member", "member@test.com", "123456", Role.MEMBER);
        member.setId(2L);

        project = Project.create("Projeto Teste", "Descricao", admin);
        project.addMember(member);
    }

    @Test
    @DisplayName("Deve permitir mudar status de TODO para IN_PROGRESS")
    void shouldChangeFromTodoToInProgress() {
        Task task = Task.create("Tarefa 1", "Desc", TaskPriority.MEDIUM, null, member);

        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        when(userRepository.findById(any())).thenReturn(Optional.of(member));
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));
        when(taskRepository.findAllByAssigneeIdAndStatus(any(), any())).thenReturn(Collections.emptyList());
        when(taskRepository.save(any())).thenReturn(task);

        Task result = useCase.execute(1L, TaskStatus.IN_PROGRESS, 1L, 2L);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    @DisplayName("Regra 1: Nao deve permitir voltar de DONE para TODO")
    void shouldNotAllowDoneToTodo() {
        Task task = Task.create("Tarefa 1", "Desc", TaskPriority.MEDIUM, null, null);
        task.changeStatus(TaskStatus.IN_PROGRESS, Role.MEMBER);
        task.changeStatus(TaskStatus.DONE, Role.MEMBER);

        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        when(userRepository.findById(any())).thenReturn(Optional.of(member));
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));

        DomainException ex = assertThrows(DomainException.class, () ->
                useCase.execute(1L, TaskStatus.TODO, 1L, 2L)
        );

        assertEquals("Tarefa DONE nao pode voltar para TODO", ex.getMessage());
    }

    @Test
    @DisplayName("Regra 2: Apenas ADMIN pode fechar tarefa CRITICAL")
    void shouldNotAllowMemberToCloseCritical() {
        Task task = Task.create("Tarefa Critica", "Desc", TaskPriority.CRITICAL, null, null);
        task.changeStatus(TaskStatus.IN_PROGRESS, Role.MEMBER);

        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        when(userRepository.findById(any())).thenReturn(Optional.of(member));
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));

        DomainException ex = assertThrows(DomainException.class, () ->
                useCase.execute(1L, TaskStatus.DONE, 1L, 2L)
        );

        assertEquals("Apenas ADMIN pode fechar tarefa CRITICAL", ex.getMessage());
    }

    @Test
    @DisplayName("Regra 2: ADMIN pode fechar tarefa CRITICAL")
    void shouldAllowAdminToCloseCritical() {
        Task task = Task.create("Tarefa Critica", "Desc", TaskPriority.CRITICAL, null, null);
        task.changeStatus(TaskStatus.IN_PROGRESS, Role.ADMIN);

        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        when(userRepository.findById(any())).thenReturn(Optional.of(admin));
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        Task result = useCase.execute(1L, TaskStatus.DONE, 1L, 1L);

        assertEquals(TaskStatus.DONE, result.getStatus());
    }

    @Test
    @DisplayName("Regra 3: Nao deve permitir mais de 5 tarefas IN_PROGRESS por responsavel")
    void shouldEnforceWipLimit() {
        Task task = Task.create("Tarefa 6", "Desc", TaskPriority.LOW, null, member);

        var inProgressTasks = java.util.List.of(
                Task.create("T1", "", TaskPriority.LOW, null, member),
                Task.create("T2", "", TaskPriority.LOW, null, member),
                Task.create("T3", "", TaskPriority.LOW, null, member),
                Task.create("T4", "", TaskPriority.LOW, null, member),
                Task.create("T5", "", TaskPriority.LOW, null, member)
        );

        when(projectRepository.findById(any())).thenReturn(Optional.of(project));
        when(userRepository.findById(any())).thenReturn(Optional.of(member));
        when(taskRepository.findById(any())).thenReturn(Optional.of(task));
        when(taskRepository.findAllByAssigneeIdAndStatus(any(), any())).thenReturn(inProgressTasks);

        DomainException ex = assertThrows(DomainException.class, () ->
                useCase.execute(1L, TaskStatus.IN_PROGRESS, 1L, 2L)
        );

        assertTrue(ex.getMessage().contains("limite WIP"));
    }
}
