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

    // ===== Self-Profile Endpoints (Any authenticated user) =====

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileDTO>> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {
        ProfileDTO profile = profileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileDTO>> updateMyProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ProfileUpdateDTO dto) {
        ProfileDTO updated = profileService.updateProfile(userId, dto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @PostMapping(value = "/profile/image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProfileDTO>> uploadProfileImage(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) {
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
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers(
            @RequestHeader("X-User-Role") String role) {
        validateAdminAccess(role);
        List<UserResponseDTO> users = profileService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users fetched successfully", users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        validateAdminAccess(role);
        UserResponseDTO user = profileService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", user));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserRole(
            @PathVariable Long id,
            @RequestParam String newRole,
            @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            throw new RuntimeException("Only Super Admins can change user roles.");
        }
        UserResponseDTO updated = profileService.updateUserRole(id, newRole);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", updated));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_SUPER_ADMIN".equals(role)) {
            throw new RuntimeException("Only Super Admins can delete user accounts.");
        }
        profileService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "User with id " + id + " has been removed."));
    }

    // ---- Helper ----
    private void validateAdminAccess(String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            throw new RuntimeException("Access denied. Admin privileges required.");
        }
    }
}
