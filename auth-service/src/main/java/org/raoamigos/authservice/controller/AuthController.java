package org.raoamigos.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raoamigos.authservice.dto.*;
import org.raoamigos.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.raoamigos.authservice.entity.User;
import org.raoamigos.authservice.repository.UserRepository;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final jakarta.servlet.http.HttpServletRequest request;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody RegisterRequestDTO dto) {
        String message = authService.register(dto);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", message));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequestDTO dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(ApiResponse.success("Login successful", token));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        String message = authService.verifyOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.success("Success", message));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam String email) {
        String message = authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("Success", message));
    }

    // ===== Password Management =====

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        String message = authService.forgotPassword(dto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Success", message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordDTO dto) {
        String message = authService.resetPassword(dto.getEmail(), dto.getOtp(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Success", message));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePasswordDTO dto) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required."));
        }
        Long userId = Long.parseLong(userIdStr);
        String message = authService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Success", message));
    }
}
