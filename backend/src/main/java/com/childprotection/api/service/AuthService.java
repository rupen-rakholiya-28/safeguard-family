package com.childprotection.api.service;

import com.childprotection.api.dto.request.LoginRequest;
import com.childprotection.api.dto.request.SignupRequest;
import com.childprotection.api.dto.response.AuthResponse;
import com.childprotection.api.model.User;
import com.childprotection.api.model.enums.UserRole;
import com.childprotection.api.repository.UserRepository;
import com.childprotection.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditLogService = auditLogService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getDisplayName(),
                UserRole.PARENT
        );
        user.setPhoneNumber(request.getPhoneNumber());
        user = userRepository.save(user);

        auditLogService.log(null, user.getId(), "SIGNUP", "USER",
                user.getId().toString(), "Parent account created");

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        auditLogService.log(null, user.getId(), "LOGIN", "USER",
                user.getId().toString(), "User logged in");

        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        var userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId().toString(), user.getEmail(),
                user.getDisplayName(), user.getRole().name());

        return new AuthResponse(accessToken, refreshToken, userInfo);
    }
}
