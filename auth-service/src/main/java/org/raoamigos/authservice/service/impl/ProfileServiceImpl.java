package org.raoamigos.authservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.raoamigos.authservice.dto.ProfileDTO;
import org.raoamigos.authservice.dto.ProfileUpdateDTO;
import org.raoamigos.authservice.dto.UserResponseDTO;
import org.raoamigos.authservice.entity.Role;
import org.raoamigos.authservice.entity.User;
import org.raoamigos.authservice.repository.UserRepository;
import org.raoamigos.authservice.service.ProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Value("${file.profile-upload-dir:profile-uploads/}")
    private String profileUploadDir;

    @Override
    public ProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToProfileDTO(user);
    }

    @Override
    public ProfileDTO updateProfile(Long userId, ProfileUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getStreet() != null) user.setStreet(dto.getStreet());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getState() != null) user.setState(dto.getState());
        if (dto.getZipCode() != null) user.setZipCode(dto.getZipCode());

        User saved = userRepository.save(user);
        log.info("Profile updated for userId={}", userId);
        return mapToProfileDTO(saved);
    }

    @Override
    public ProfileDTO uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        try {
            Path uploadPath = Paths.get(profileUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String fileName = "profile_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setProfileImagePath(fileName);
            User saved = userRepository.save(user);
            log.info("Profile image uploaded for userId={}, fileName={}", userId, fileName);
            return mapToProfileDTO(saved);

        } catch (IOException e) {
            log.error("Failed to upload profile image for userId={}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to upload profile image: " + e.getMessage());
        }
    }

    @Override
    public ProfileDTO deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getProfileImagePath() != null) {
            try {
                Path filePath = Paths.get(profileUploadDir).resolve(user.getProfileImagePath());
                Files.deleteIfExists(filePath);
                log.info("Profile image file deleted for userId={}", userId);
            } catch (IOException e) {
                log.warn("Failed to delete profile image file for userId={}: {}", userId, e.getMessage());
            }
            user.setProfileImagePath(null);
            user = userRepository.save(user);
            log.info("Profile image path cleared in DB for userId={}", userId);
        }
        return mapToProfileDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUserRole(Long targetUserId, String newRole) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + targetUserId));

        Role role = Role.valueOf(newRole);
        user.setRole(role);
        User saved = userRepository.save(user);
        return mapToUserResponseDTO(saved);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new RuntimeException("Cannot delete a Super Admin account.");
        }

        userRepository.delete(user);
    }

    @Override
    public UserResponseDTO toggleUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new RuntimeException("Cannot deactivate a Super Admin account.");
        }

        user.setActive(active);
        User saved = userRepository.save(user);
        return mapToUserResponseDTO(saved);
    }

    // ---- Mapping Helpers ----

    private ProfileDTO mapToProfileDTO(User user) {
        return ProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .street(user.getStreet())
                .city(user.getCity())
                .state(user.getState())
                .zipCode(user.getZipCode())
                .profileImagePath(user.getProfileImagePath())
                .active(user.isActive())
                .build();
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .city(user.getCity())
                .state(user.getState())
                .profileImagePath(user.getProfileImagePath())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .build();
    }
}
