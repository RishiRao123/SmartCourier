package org.raoamigos.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.authservice.dto.ApiResponse;
import org.raoamigos.authservice.dto.ProfileDTO;
import org.raoamigos.authservice.dto.ProfileUpdateDTO;
import org.raoamigos.authservice.dto.UserResponseDTO;
import org.raoamigos.authservice.service.ProfileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final org.raoamigos.authservice.util.JwtUtil jwtUtil;

    // ===== Self-Profile Endpoints (Any authenticated user) =====

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileDTO>> getMyProfile(jakarta.servlet.http.HttpServletRequest request) {
        Long userId = getUserIdFromHeader(request);
        ProfileDTO profile = profileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileDTO>> updateMyProfile(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestBody ProfileUpdateDTO dto) {
        Long userId = getUserIdFromHeader(request);
        ProfileDTO updated = profileService.updateProfile(userId, dto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @PostMapping(value = "/profile/image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProfileDTO>> uploadProfileImage(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        Long userId = getUserIdFromHeader(request);
        ProfileDTO updated = profileService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(ApiResponse.success("Profile image uploaded successfully", updated));
    }

    @GetMapping("/profile/image/{filename}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("profile-uploads/").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== Admin User Management Endpoints =====

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers(jakarta.servlet.http.HttpServletRequest request) {
        validateAdminAccess(request);
        List<UserResponseDTO> users = profileService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users fetched successfully", users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            jakarta.servlet.http.HttpServletRequest request,
            @PathVariable Long id) {
        validateAdminAccess(request);
        UserResponseDTO user = profileService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", user));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserRole(
            jakarta.servlet.http.HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam String newRole) {
        String role = getRoleFromHeader(request);
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            throw new RuntimeException("Only Super Admins can change user roles.");
        }
        UserResponseDTO updated = profileService.updateUserRole(id, newRole);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", updated));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            jakarta.servlet.http.HttpServletRequest request,
            @PathVariable Long id) {
        String role = getRoleFromHeader(request);
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            throw new RuntimeException("Only Super Admins can delete user accounts.");
        }
        profileService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "User with id " + id + " has been removed."));
    }

    // ===== Activation Management (SUPER_ADMIN only) =====

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponseDTO>> toggleUserActive(
            jakarta.servlet.http.HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam boolean active) {
        String role = getRoleFromHeader(request);
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            throw new RuntimeException("Only Super Admins can activate/deactivate accounts.");
        }
        UserResponseDTO updated = profileService.toggleUserActive(id, active);
        String action = active ? "activated" : "deactivated";
        return ResponseEntity.ok(ApiResponse.success("User " + action + " successfully", updated));
    }

    // ---- Helpers ----
    private Long getUserIdFromHeader(jakarta.servlet.http.HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        
        // Fallback to JWT if X-User-Id is missing (e.g., direct call or Gateway config issue)
        if (userIdStr == null || userIdStr.isBlank() || "null".equals(userIdStr)) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Long id = jwtUtil.extractUserId(token);
                    if (id != null) return id;
                } catch (Exception e) {
                    // fall through
                }
            }
            throw new RuntimeException("User Identity missing in request. Header X-User-Id was null or empty.");
        }

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid User Identity format: '" + userIdStr + "'. Must be a number.");
        }
    }

    private String getRoleFromHeader(jakarta.servlet.http.HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        
        if (role == null || role.isBlank() || "null".equals(role)) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String extractedRole = jwtUtil.extractRole(token);
                    if (extractedRole != null) return extractedRole;
                } catch (Exception e) {
                    // fall through
                }
            }
            throw new RuntimeException("User Role missing in request header X-User-Role.");
        }
        return role;
    }

    private void validateAdminAccess(jakarta.servlet.http.HttpServletRequest request) {
        String role = getRoleFromHeader(request);
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }
    }
}
