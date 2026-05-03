package org.raoamigos.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raoamigos.authservice.dto.ApiResponse;
import org.raoamigos.authservice.dto.RegisterRequestDTO;
import org.raoamigos.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth-admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;
    private final jakarta.servlet.http.HttpServletRequest request;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> adminSignup(
            @Valid @RequestBody RegisterRequestDTO dto) {
        
        String role = request.getHeader("X-User-Role");
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only Super Admins can create new admin accounts"));
        }
        
        String message = authService.registerAdmin(dto);
        return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", message));
    }
}
