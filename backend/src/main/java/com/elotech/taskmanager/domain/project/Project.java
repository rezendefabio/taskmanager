package com.elotech.taskmanager.domain.project;

import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.user.Role;
import com.elotech.taskmanager.domain.user.User;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    protected Project() {}

    private Project(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.members.add(owner);
    }

    public static Project create(String name, String description, User owner) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Nome do projeto e obrigatorio");
        }

        if (owner == null) {
            throw new DomainException("Projeto precisa de um dono");
        }
        return new Project(name.trim(), description, owner);
    }

    public void addMember(User user) {
        if (user == null) {
            throw new DomainException("Usuario e obrigatorio");
        }
        this.members.add(user);
    }

    public void removeMember(User user) {
        if (user.getId().equals(this.owner.getId())) {
            throw new DomainException("Nao e possível remover o dono do projeto");
        }
        this.members.remove(user);
    }

    public boolean isMember(User user) {
        return this.members.stream()
                .anyMatch(m -> m.getId().equals(user.getId()));
    }

    public Role getRoleOf(User user) {
        if (user.getId().equals(this.owner.getId())) {
            return Role.ADMIN;
        }
        if (isMember(user)) {
            return  Role.MEMBER;
        }

        throw new DomainException("Usuario nao pertence a este projeto");
    }

    public void update(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Nome do projeto e obrigatorio");
        }
        this.name = name.trim();
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public User getOwner() {
        return owner;
    }

    public Set<User> getMembers() {
        return members;
    }
}
