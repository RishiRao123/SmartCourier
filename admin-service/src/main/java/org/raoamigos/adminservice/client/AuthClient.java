package org.raoamigos.adminservice.client;

import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {

    @GetMapping("/auth/users")
    ApiResponse<List<UserDTO>> getAllUsers(@RequestHeader("X-User-Role") String role);

    @GetMapping("/auth/users/{id}")
    ApiResponse<UserDTO> getUserById(@PathVariable("id") Long id, @RequestHeader("X-User-Role") String role);

    @PutMapping("/auth/users/{id}/role")
    ApiResponse<UserDTO> updateUserRole(@PathVariable("id") Long id, @RequestParam("newRole") String newRole, @RequestHeader("X-User-Role") String role);

    @DeleteMapping("/auth/users/{id}")
    ApiResponse<String> deleteUser(@PathVariable("id") Long id, @RequestHeader("X-User-Role") String role);

    @GetMapping("/auth/profile")
    ApiResponse<UserDTO> getProfile(@RequestHeader("X-User-Id") Long userId);
}
