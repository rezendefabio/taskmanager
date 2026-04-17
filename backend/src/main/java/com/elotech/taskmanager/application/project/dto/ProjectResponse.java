package com.elotech.taskmanager.application.project.dto;

import com.elotech.taskmanager.domain.project.Project;

import java.util.Set;
import java.util.stream.Collectors;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        String ownerName,
        Set<MemberResponse> members
) {
    public record MemberResponse(Long id, String name) {}

    public static ProjectResponse fromEntity(Project project) {
        Set<MemberResponse> memberList = project.getMembers().stream()
                .map(user -> new MemberResponse(user.getId(), user.getName()))
                .collect(Collectors.toSet());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getName(),
                memberList
        );
    }
}
