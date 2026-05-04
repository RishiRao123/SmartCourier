package org.raoamigos.authservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.authservice.config.RabbitMQConfig;
import org.raoamigos.authservice.dto.*;
import org.raoamigos.authservice.entity.*;
import org.raoamigos.authservice.repository.OtpVerificationRepository;
import org.raoamigos.authservice.repository.UserRepository;
import org.raoamigos.authservice.util.JwtUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase 1 — AuthServiceImpl Unit Tests (14 scenarios)
 * Covers: registration, OTP lifecycle, login gate, password flows, admin registration.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpVerificationRepository otpVerificationRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // ─── Builder Helpers ──────────────────────────────────────────────────────

    private RegisterRequestDTO buildRegisterDTO() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@smartcourier.com");
        dto.setPassword("Secret123!");
        dto.setPhone("9876543210");
        dto.setStreet("1 Test St");
        dto.setCity("Mumbai");
        dto.setState("MH");
        dto.setZipCode("400001");
        return dto;
    }

    private User buildActiveCustomer() {
        return User.builder()
                .id(1L).username("testuser")
                .email("test@smartcourier.com")
                .password("encodedPassword")
                .role(Role.ROLE_CUSTOMER)
                .active(true)
                .build();
    }

    private User buildInactiveCustomer() {
        return User.builder()
                .id(2L).username("newuser")
                .email("new@smartcourier.com")
                .password("encodedPassword")
                .role(Role.ROLE_CUSTOMER)
                .active(false)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // REGISTER
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 1: register() saves inactive user, persists OTP with SIGNUP_OTP purpose, publishes 6-digit OtpEvent")
    void register_HappyPath_ShouldSaveUserAndPublishOtpEvent() {
        RegisterRequestDTO dto = buildRegisterDTO();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = authService.register(dto);

        // User persisted
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isActive(), "New customer must start inactive");
        assertEquals(Role.ROLE_CUSTOMER, userCaptor.getValue().getRole());

        // Old OTP cleared
        verify(otpVerificationRepository).deleteByEmailAndPurpose(dto.getEmail(), OtpPurpose.SIGNUP_OTP);

        // OTP record saved
        ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(otpCaptor.capture());
        OtpVerification saved = otpCaptor.getValue();
        assertEquals(OtpPurpose.SIGNUP_OTP, saved.getPurpose());
        assertTrue(saved.getOtp().matches("\\d{6}"),
                "OTP must be exactly 6 numeric digits, got: " + saved.getOtp());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now().plusSeconds(550)));

        // OtpEvent published with correct routing
        ArgumentCaptor<OtpEvent> eventCaptor = ArgumentCaptor.forClass(OtpEvent.class);
        verify(rabbitTemplate).convertAndSend(
                (String) eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                (String) eq(RabbitMQConfig.OTP_ROUTING_KEY),
                (Object) eventCaptor.capture());
        assertEquals(dto.getEmail(), eventCaptor.getValue().getEmail());
        assertEquals(saved.getOtp(), eventCaptor.getValue().getOtp());

        assertTrue(result.contains("OTP sent to email"));
    }

    @Test
    @DisplayName("Scenario 2: register() throws 'Email already exists' when email is taken")
    void register_WhenEmailTaken_ShouldThrowException() {
        RegisterRequestDTO dto = buildRegisterDTO();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(buildActiveCustomer()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(dto));
        assertEquals("Email already exists", ex.getMessage());
        verify(rabbitTemplate, never()).convertAndSend(
                (String) anyString(), 
                (String) anyString(), 
                (Object) any());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VERIFY OTP
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 3: verifyOtp() activates user and marks OTP verified")
    void verifyOtp_HappyPath_ShouldActivateUserAndMarkOtpVerified() {
        User user = buildInactiveCustomer();
        OtpVerification validOtp = OtpVerification.builder()
                .email(user.getEmail()).otp("123456")
                .expiresAt(Instant.now().plusSeconds(300))
                .purpose(OtpPurpose.SIGNUP_OTP).verified(false)
                .build();

        when(otpVerificationRepository.findByEmailAndOtpAndPurpose(
                user.getEmail(), "123456", OtpPurpose.SIGNUP_OTP))
                .thenReturn(Optional.of(validOtp));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String result = authService.verifyOtp(user.getEmail(), "123456");

        assertTrue(user.isActive(), "User should be activated");
        assertTrue(validOtp.isVerified(), "OTP should be marked verified");
        assertTrue(result.contains("OTP verified successfully"));
    }

    @Test
    @DisplayName("Scenario 4: verifyOtp() throws 'OTP has expired' for past-expiry OTP")
    void verifyOtp_WhenOtpExpired_ShouldThrowException() {
        OtpVerification expiredOtp = OtpVerification.builder()
                .email("u@t.com").otp("654321")
                .expiresAt(Instant.now().minusSeconds(60))
                .purpose(OtpPurpose.SIGNUP_OTP)
                .build();
        when(otpVerificationRepository.findByEmailAndOtpAndPurpose(
                "u@t.com", "654321", OtpPurpose.SIGNUP_OTP))
                .thenReturn(Optional.of(expiredOtp));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.verifyOtp("u@t.com", "654321"));
        assertEquals("OTP has expired", ex.getMessage());
    }

    @Test
    @DisplayName("Scenario 5: verifyOtp() throws 'Invalid OTP' when record not found")
    void verifyOtp_WhenOtpNotFound_ShouldThrowException() {
        when(otpVerificationRepository.findByEmailAndOtpAndPurpose(
                anyString(), anyString(), eq(OtpPurpose.SIGNUP_OTP)))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.verifyOtp("ghost@t.com", "000000"));
        assertEquals("Invalid OTP", ex.getMessage());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESEND OTP
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 6: resendOtp() updates existing OTP record and republishes OtpEvent")
    void resendOtp_HappyPath_ShouldUpdateOtpAndPublishEvent() {
        User user = buildInactiveCustomer();
        OtpVerification existing = OtpVerification.builder()
                .email(user.getEmail()).otp("111111")
                .expiresAt(Instant.now().plusSeconds(60))
                .purpose(OtpPurpose.SIGNUP_OTP)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otpVerificationRepository.findByEmailAndPurpose(user.getEmail(), OtpPurpose.SIGNUP_OTP))
                .thenReturn(Optional.of(existing));

        String result = authService.resendOtp(user.getEmail());

        verify(otpVerificationRepository).save(existing);
        assertTrue(existing.getOtp().matches("\\d{6}"), "Updated OTP should be 6 digits");
        verify(rabbitTemplate).convertAndSend(
                (String) eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                (String) eq(RabbitMQConfig.OTP_ROUTING_KEY),
                (Object) any(OtpEvent.class));
        assertEquals("OTP resent successfully", result);
    }

    @Test
    @DisplayName("Scenario 7: resendOtp() throws 'User is already active' for active users")
    void resendOtp_WhenUserAlreadyActive_ShouldThrowException() {
        when(userRepository.findByEmail("test@smartcourier.com"))
                .thenReturn(Optional.of(buildActiveCustomer()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.resendOtp("test@smartcourier.com"));
        assertEquals("User is already active", ex.getMessage());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 8: login() returns 'token: <jwt>' for an active user")
    void login_HappyPath_ShouldReturnJwtToken() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@smartcourier.com");
        dto.setPassword("Secret123!");
        User user = buildActiveCustomer();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(), user.getUsername()))
                .thenReturn("mocked.jwt.token");

        String result = authService.login(dto);

        assertTrue(result.startsWith("token: "));
        assertTrue(result.contains("mocked.jwt.token"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Scenario 9: login() throws 'verify your email' for inactive ROLE_CUSTOMER")
    void login_WhenCustomerInactive_ShouldThrowException() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("new@smartcourier.com");
        dto.setPassword("Secret123!");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(buildInactiveCustomer()));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(dto));
        assertTrue(ex.getMessage().contains("verify your email"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FORGOT PASSWORD
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 10: forgotPassword() deletes old OTP, saves new 15-min OTP, publishes PasswordResetEvent")
    void forgotPassword_HappyPath_ShouldPublishPasswordResetEvent() {
        User user = buildActiveCustomer();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        String result = authService.forgotPassword(user.getEmail());

        verify(otpVerificationRepository).deleteByEmailAndPurpose(user.getEmail(), OtpPurpose.PASSWORD_RESET);

        ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(otpCaptor.capture());
        OtpVerification saved = otpCaptor.getValue();
        assertEquals(OtpPurpose.PASSWORD_RESET, saved.getPurpose());
        assertTrue(saved.getOtp().matches("\\d{6}"));
        // Expiry ~900 seconds — must be after now+850s
        assertTrue(saved.getExpiresAt().isAfter(Instant.now().plusSeconds(850)));

        ArgumentCaptor<PasswordResetEvent> evCaptor = ArgumentCaptor.forClass(PasswordResetEvent.class);
        verify(rabbitTemplate).convertAndSend(
                (String) eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                (String) eq(RabbitMQConfig.PASSWORD_RESET_ROUTING_KEY),
                (Object) evCaptor.capture());
        assertEquals(user.getEmail(), evCaptor.getValue().getEmail());
        assertEquals(saved.getOtp(), evCaptor.getValue().getOtp());

        assertTrue(result.contains("If this email is registered"));
    }

    @Test
    @DisplayName("Scenario 11: forgotPassword() returns silent success for unknown email (anti-enumeration)")
    void forgotPassword_WhenEmailNotFound_ShouldReturnSilentSuccess() {
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());

        String result = authService.forgotPassword("ghost@t.com");

        verify(rabbitTemplate, never()).convertAndSend(
                (String) anyString(), 
                (String) anyString(), 
                (Object) any());
        assertTrue(result.contains("If this email is registered"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESET PASSWORD
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 12: resetPassword() encodes new password, marks OTP verified, deletes OTP")
    void resetPassword_HappyPath_ShouldUpdatePasswordAndCleanUp() {
        User user = buildActiveCustomer();
        OtpVerification validOtp = OtpVerification.builder()
                .email(user.getEmail()).otp("999888")
                .expiresAt(Instant.now().plusSeconds(300))
                .purpose(OtpPurpose.PASSWORD_RESET)
                .verified(false)
                .build();

        when(otpVerificationRepository.findByEmailAndOtpAndPurpose(
                user.getEmail(), "999888", OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(validOtp));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword!")).thenReturn("newEncodedPassword");

        String result = authService.resetPassword(user.getEmail(), "999888", "NewPassword!");

        verify(userRepository).save(user);
        assertEquals("newEncodedPassword", user.getPassword());
        assertTrue(validOtp.isVerified());
        verify(otpVerificationRepository).deleteByEmailAndPurpose(user.getEmail(), OtpPurpose.PASSWORD_RESET);
        assertTrue(result.contains("Password reset successfully"));
    }

    @Test
    @DisplayName("Scenario 13: resetPassword() throws when OTP is expired")
    void resetPassword_WhenOtpExpired_ShouldThrowException() {
        OtpVerification expiredOtp = OtpVerification.builder()
                .email("u@t.com").otp("777666")
                .expiresAt(Instant.now().minusSeconds(30))
                .purpose(OtpPurpose.PASSWORD_RESET)
                .build();
        when(otpVerificationRepository.findByEmailAndOtpAndPurpose(
                "u@t.com", "777666", OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(expiredOtp));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.resetPassword("u@t.com", "777666", "newPass"));
        assertTrue(ex.getMessage().contains("expired"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // REGISTER ADMIN
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 14: registerAdmin() saves active admin and publishes AdminCredentialsEvent")
    void registerAdmin_HappyPath_ShouldSaveAdminAndPublishCredentials() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("adminUser");
        dto.setEmail("admin@smartcourier.com");
        dto.setPassword("ignored");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedAutoPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = authService.registerAdmin(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isActive(), "Admin should be active=true on creation");
        assertEquals(Role.ROLE_ADMIN, userCaptor.getValue().getRole());

        ArgumentCaptor<AdminCredentialsEvent> evCaptor =
                ArgumentCaptor.forClass(AdminCredentialsEvent.class);
        verify(rabbitTemplate).convertAndSend(
                (String) eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                (String) eq(RabbitMQConfig.ADMIN_CREDENTIALS_ROUTING_KEY),
                (Object) evCaptor.capture());
        assertEquals(dto.getEmail(), evCaptor.getValue().getEmail());
        assertEquals(dto.getUsername(), evCaptor.getValue().getUsername());
        assertNotNull(evCaptor.getValue().getRawPassword());

        assertTrue(result.contains("Admin registration submitted"));
    }
}