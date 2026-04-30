package org.raoamigos.authservice.service;

import org.raoamigos.authservice.dto.ProfileDTO;
import org.raoamigos.authservice.dto.ProfileUpdateDTO;
import org.raoamigos.authservice.dto.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {

    ProfileDTO getProfile(Long userId);

    ProfileDTO updateProfile(Long userId, ProfileUpdateDTO dto);

    ProfileDTO uploadProfileImage(Long userId, MultipartFile file);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO updateUserRole(Long targetUserId, String newRole);

    void deleteUser(Long userId);
}
