package com.elotech.taskmanager.domain.user;

import com.elotech.taskmanager.domain.shared.DomainException;
import jakarta.persistence.*;

import java.util.Locale;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected User() {}

    private User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User create(String name, String email, String password, Role role) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Nome é obrigatório");
        }

        if (email == null || email.isBlank()) {
            throw new DomainException("E-mail é obrigatório");
        }

        if (password == null || password.isBlank()) {
            throw new DomainException("Senha é obrigatória");
        }

        if (role == null) {
            throw new DomainException("Role é obrigatória");
        }

        return new User(name.trim(), email.trim().toLowerCase(), password, role);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
