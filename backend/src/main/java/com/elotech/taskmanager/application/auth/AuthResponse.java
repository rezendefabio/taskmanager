package com.elotech.taskmanager.application.auth;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String role
) {}
