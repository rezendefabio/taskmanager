package com.elotech.taskmanager.infrastructure.persistence;

import com.elotech.taskmanager.domain.task.Task;
import com.elotech.taskmanager.domain.task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findAllByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);
}
