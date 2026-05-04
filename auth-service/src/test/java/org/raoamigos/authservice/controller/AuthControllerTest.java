package org.raoamigos.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.raoamigos.authservice.dto.*;
import org.raoamigos.authservice.repository.UserRepository;
import org.raoamigos.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Phase 2 — AuthController @WebMvcTest (6 scenarios)
 *
 * Uses the real SecurityConfig but all /auth/** endpoints are permitAll(),
 * so no authentication setup is required.
 *
 * NOTE: We import SecurityConfig so the full filter chain is applied.
 * If SecurityConfig pulls in CustomUserDetailsService, MockBean it below.
 */
@WebMvcTest(AuthController.class)
@Import(org.raoamigos.authservice.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private UserRepository userRepository;
    // Required by SecurityConfig → CustomUserDetailsService
    @MockBean private org.raoamigos.authservice.config.CustomUserDetailsService customUserDetailsService;

    // ─── Scenario 1 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 1: POST /auth/signup returns 200 with success ApiResponse wrapper")
    void signup_HappyPath_ShouldReturn200WithSuccessWrapper() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@smartcourier.com");
        dto.setPassword("Secret123!");
        dto.setPhone("9876543210");
        dto.setStreet("1 St"); dto.setCity("Mumbai");
        dto.setState("MH"); dto.setZipCode("400001");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenReturn("User registered successfully. Please verify your OTP sent to email.");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data").isString());
    }

    // ─── Scenario 2 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 2: POST /auth/signup returns 400 when required fields are blank")
    void signup_WhenRequiredFieldsMissing_ShouldReturn400() throws Exception {
        // Empty DTO — all @NotBlank constraints will fail
        RegisterRequestDTO dto = new RegisterRequestDTO();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ─── Scenario 3 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 3: POST /auth/login returns 200 with token in data field")
    void login_HappyPath_ShouldReturn200WithToken() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@smartcourier.com");
        dto.setPassword("Secret123!");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn("token: mocked.jwt.token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data").value("token: mocked.jwt.token"));
    }

    // ─── Scenario 4 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 4: POST /auth/verify-otp returns 200 with email and otp as @RequestParam")
    void verifyOtp_HappyPath_ShouldReturn200() throws Exception {
        when(authService.verifyOtp("test@smartcourier.com", "123456"))
                .thenReturn("OTP verified successfully. You can now log in.");

        mockMvc.perform(post("/auth/verify-otp")
                        .param("email", "test@smartcourier.com")
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("OTP verified successfully. You can now log in."));
    }

    // ─── Scenario 5 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 5: POST /auth/forgot-password always returns 200 (anti-enumeration)")
    void forgotPassword_ShouldAlwaysReturn200() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setEmail("anyone@test.com");

        when(authService.forgotPassword(anyString()))
                .thenReturn("If this email is registered, you will receive a password reset code.");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isString());
    }

    // ─── Scenario 6 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Scenario 6: PUT /auth/change-password returns 401 when X-User-Id header is missing")
    void changePassword_WhenHeaderMissing_ShouldReturn401() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("old");
        dto.setNewPassword("new");

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // No X-User-Id header
                .andExpect(status().isForbidden());
    }
}