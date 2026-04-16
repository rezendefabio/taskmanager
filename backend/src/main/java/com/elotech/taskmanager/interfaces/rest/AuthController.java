package com.elotech.taskmanager.interfaces.rest;

import com.elotech.taskmanager.application.auth.AuthResponse;
import com.elotech.taskmanager.application.auth.LoginRequest;
import com.elotech.taskmanager.application.auth.RegisterRequest;
import com.elotech.taskmanager.domain.shared.DomainException;
import com.elotech.taskmanager.domain.user.Role;
import com.elotech.taskmanager.domain.user.User;
import com.elotech.taskmanager.infrastructure.persistence.UserRepository;
import com.elotech.taskmanager.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DomainException("Email ja cadastrado");
        }

        Role role = Role.MEMBER;
        if (request.role() != null && request.role().equalsIgnoreCase("ADMIN")) {
            role = Role.ADMIN;
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = User.create(request.name(), request.email(), hashedPassword, role);
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getId(), user.getName(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new DomainException("Email ou Senha invalidos"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new DomainException("Email ou Senha invalidos");
        }

        String token = jwtService.generateToken(user.getId(), user.getName(), user.getRole().name());

        return ResponseEntity.ok(
                new AuthResponse(token, user.getId(), user.getName(), user.getRole().name())
        );
    }
}
