package com.almas.jobflow.service;

import com.almas.jobflow.dto.AuthResponse;
import com.almas.jobflow.dto.LoginRequest;
import com.almas.jobflow.dto.RegisterRequest;
import com.almas.jobflow.exception.EmailAlreadyExistsException;
import com.almas.jobflow.exception.InvalidCredentialsException;
import com.almas.jobflow.model.User;
import com.almas.jobflow.repository.UserRepository;
import com.almas.jobflow.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        return new AuthResponse(jwtUtil.generateToken(user.getEmail()), user.getEmail(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        return new AuthResponse(jwtUtil.generateToken(user.getEmail()), user.getEmail(), user.getUsername());
    }
}