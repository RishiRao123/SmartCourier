package org.raoamigos.authservice.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.authservice.dto.ProfileDTO;
import org.raoamigos.authservice.dto.ProfileUpdateDTO;
import org.raoamigos.authservice.dto.UserResponseDTO;
import org.raoamigos.authservice.entity.Role;
import org.raoamigos.authservice.entity.User;
import org.raoamigos.authservice.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Phase 1 — ProfileServiceImpl Unit Tests (7 scenarios)
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private ProfileServiceImpl profileService;

    private User buildUser(Long id, Role role, boolean active) {
        return User.builder()
                .id(id).username("user" + id)
                .email("user" + id + "@test.com")
                .password("encoded").role(role)
                .phone("9999999999").street("1 St").city("Mumbai")
                .state("MH").zipCode("400001").active(active)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }

    // ─── Scenario 1 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 1: getProfile() maps all User fields to ProfileDTO correctly")
    void getProfile_HappyPath_ShouldMapAllFieldsToProfileDTO() {
        User user = buildUser(1L, Role.ROLE_CUSTOMER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ProfileDTO dto = profileService.getProfile(1L);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getRole().name(), dto.getRole());
        assertEquals(user.getCity(), dto.getCity());
        assertTrue(dto.isActive());
    }

    // ─── Scenario 2 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 2: getProfile() throws RuntimeException when user not found")
    void getProfile_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileService.getProfile(99L));
        assertTrue(ex.getMessage().contains("User not found with id: 99"));
    }

    // ─── Scenario 3 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 3: updateProfile() applies only non-null/non-blank fields from DTO")
    void updateProfile_ShouldApplyOnlyNonNullFields() {
        User user = buildUser(1L, Role.ROLE_CUSTOMER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setCity("Delhi"); // Only city updated; other fields null/blank

        ProfileDTO result = profileService.updateProfile(1L, dto);

        assertEquals("Delhi", result.getCity());
        // Username was NOT changed (null in DTO)
        assertEquals(user.getUsername(), result.getUsername());
    }

    // ─── Scenario 4 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 4: deleteUser() calls repository.delete() for non-SUPER_ADMIN")
    void deleteUser_HappyPath_ShouldCallRepositoryDelete() {
        User user = buildUser(2L, Role.ROLE_CUSTOMER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> profileService.deleteUser(2L));
        verify(userRepository, times(1)).delete(user);
    }

    // ─── Scenario 5 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 5: deleteUser() throws 'Cannot delete a Super Admin account.'")
    void deleteUser_WhenTargetIsSuperAdmin_ShouldThrowException() {
        User superAdmin = buildUser(1L, Role.ROLE_SUPER_ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(superAdmin));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileService.deleteUser(1L));
        assertEquals("Cannot delete a Super Admin account.", ex.getMessage());
        verify(userRepository, never()).delete(any());
    }

    // ─── Scenario 6 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 6: toggleUserActive() flips the active flag and saves")
    void toggleUserActive_HappyPath_ShouldFlipActiveFlagAndSave() {
        User user = buildUser(3L, Role.ROLE_CUSTOMER, true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = profileService.toggleUserActive(3L, false);

        assertFalse(result.isActive());
        verify(userRepository).save(user);
    }

    // ─── Scenario 7 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 7: toggleUserActive() throws 'Cannot deactivate a Super Admin account.'")
    void toggleUserActive_WhenTargetIsSuperAdmin_ShouldThrowException() {
        User superAdmin = buildUser(1L, Role.ROLE_SUPER_ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(superAdmin));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileService.toggleUserActive(1L, false));
        assertEquals("Cannot deactivate a Super Admin account.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}
