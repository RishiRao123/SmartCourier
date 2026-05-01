package org.raoamigos.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raoamigos.authservice.dto.ApiResponse;
import org.raoamigos.authservice.dto.LoginRequestDTO;
import org.raoamigos.authservice.dto.RegisterRequestDTO;
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

    @PostMapping("/admin/signup")
    public ResponseEntity<ApiResponse<String>> adminSignup(
            @Valid @RequestBody RegisterRequestDTO dto) {
        
        String role = request.getHeader("X-User-Role");
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only Super Admins can create new admin accounts"));
        }
        
        String message = authService.registerAdmin(dto);
        return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", message));
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(@RequestHeader("X-User-Email") String email) {
        User user = authService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @RequestHeader("X-User-Email") String email,
            @RequestBody User updatedUser) {
        User user = authService.getUserProfile(email);
        user.setPhone(updatedUser.getPhone());
        user.setStreet(updatedUser.getStreet());
        user.setCity(updatedUser.getCity());
        user.setState(updatedUser.getState());
        user.setZipCode(updatedUser.getZipCode());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", user));
    }
}
