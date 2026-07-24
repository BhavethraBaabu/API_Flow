package com.apiflow.authentication.service;

import com.apiflow.authentication.dto.AuthResponse;
import com.apiflow.authentication.dto.LoginRequest;
import com.apiflow.authentication.dto.RefreshRequest;
import com.apiflow.authentication.dto.RegisterRequest;
import com.apiflow.authentication.entity.Role;
import com.apiflow.authentication.entity.User;
import com.apiflow.authentication.repository.UserRepository;
import com.apiflow.authentication.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role() != null ? request.role() : Role.ENGINEER)
                .enabled(true)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + request.email()));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String email = jwtService.extractUsername(request.refreshToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
