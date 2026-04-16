package com.elotech.taskmanager.infrastructure.persistence;

import com.elotech.taskmanager.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findAllByMemberId(Long userId);
}
