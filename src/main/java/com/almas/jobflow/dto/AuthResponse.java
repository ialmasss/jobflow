package com.almas.jobflow.dto;

public record AuthResponse(String token, String email, String username) {
}