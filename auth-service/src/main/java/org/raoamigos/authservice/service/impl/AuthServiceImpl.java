package org.raoamigos.authservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.raoamigos.authservice.dto.LoginRequestDTO;
import org.raoamigos.authservice.dto.RegisterRequestDTO;
import org.raoamigos.authservice.entity.Role;
import org.raoamigos.authservice.entity.User;
import org.raoamigos.authservice.repository.UserRepository;
import org.raoamigos.authservice.service.AuthService;
import org.raoamigos.authservice.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.raoamigos.authservice.entity.OtpVerification;
import org.raoamigos.authservice.entity.OtpPurpose;
import org.raoamigos.authservice.repository.OtpVerificationRepository;
import org.raoamigos.authservice.dto.OtpEvent;
import org.raoamigos.authservice.dto.PasswordResetEvent;
import org.raoamigos.authservice.config.RabbitMQConfig;

import java.time.Instant;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final OtpVerificationRepository otpVerificationRepository;

    private String generateOtp() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        return String.valueOf(otpValue);
    }

    @org.springframework.transaction.annotation.Transactional
    public String register(RegisterRequestDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .role(Role.ROLE_CUSTOMER)
                .active(false) // Customers are inactive until OTP is verified
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        // Generate OTP
        String otp = generateOtp();
        log.debug("Generated OTP for {}: {}", user.getEmail(), otp);
        
        // Remove existing signup OTP if any
        otpVerificationRepository.deleteByEmailAndPurpose(user.getEmail(), OtpPurpose.SIGNUP_OTP);

        // Save OTP with SIGNUP_OTP purpose
        OtpVerification otpVerification = OtpVerification.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(600))
                .purpose(OtpPurpose.SIGNUP_OTP)
                .build();
        otpVerificationRepository.save(otpVerification);

        // Send OTP via RabbitMQ
        OtpEvent otpEvent = new OtpEvent(user.getEmail(), otp);
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.OTP_ROUTING_KEY, otpEvent);

        return "User registered successfully. Please verify your OTP sent to email.";
    }

    public String login(LoginRequestDTO dto) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getEmail(),
                dto.getPassword()
        ));
        log.info("Authentication successful for user: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Block inactive users from logging in
        if (!user.isActive()) {
            if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
                // Should not happen for super admin, but just in case
                user.setActive(true);
                userRepository.save(user);
            } else if (user.getRole() == Role.ROLE_CUSTOMER) {
                throw new RuntimeException("Please verify your email address before logging in.");
            } else {
                throw new RuntimeException("Your account is pending approval from a Super Admin. Please wait for activation.");
            }
        }

        // Include username in the JWT for frontend display
        return "token: " + jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(), user.getUsername());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String registerAdmin(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Generate a random secure password for the new admin
        String rawPassword = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ROLE_ADMIN)
                .active(true) // Admin accounts are active by default
                .build();

        userRepository.save(user);
        log.info("Admin registered: email={}, username={}", user.getEmail(), user.getUsername());

        // Send credentials via RabbitMQ
        org.raoamigos.authservice.dto.AdminCredentialsEvent event = new org.raoamigos.authservice.dto.AdminCredentialsEvent(
                user.getEmail(),
                user.getUsername(),
                rawPassword
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.ADMIN_CREDENTIALS_ROUTING_KEY, event);
        log.info("Admin credentials event published for: {}", user.getEmail());

        return "Admin registration submitted successfully. Credentials have been emailed.";
    }

    @Override
    public User getUserProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public String verifyOtp(String email, String otp) {
        OtpVerification otpVerification = otpVerificationRepository.findByEmailAndOtpAndPurpose(email, otp, OtpPurpose.SIGNUP_OTP)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (otpVerification.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP has expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
        userRepository.save(user);

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        return "OTP verified successfully. You can now log in.";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isActive()) {
            throw new RuntimeException("User is already active");
        }

        String otp = generateOtp();
        
        OtpVerification existingOtp = otpVerificationRepository.findByEmailAndPurpose(email, OtpPurpose.SIGNUP_OTP).orElse(null);
        if (existingOtp != null) {
            existingOtp.setOtp(otp);
            existingOtp.setExpiresAt(Instant.now().plusSeconds(600));
            otpVerificationRepository.save(existingOtp);
        } else {
            OtpVerification newOtp = OtpVerification.builder()
                    .email(user.getEmail())
                    .otp(otp)
                    .expiresAt(Instant.now().plusSeconds(600))
                    .purpose(OtpPurpose.SIGNUP_OTP)
                    .build();
            otpVerificationRepository.save(newOtp);
        }

        OtpEvent otpEvent = new OtpEvent(user.getEmail(), otp);
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.OTP_ROUTING_KEY, otpEvent);

        return "OTP resent successfully";
    }

    // ===== Password Management =====

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String forgotPassword(String email) {
        // Always return success to prevent email enumeration
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "If this email is registered, you will receive a password reset code.";
        }

        String otp = generateOtp();
        log.debug("Password reset OTP for {}: {}", email, otp);

        // Remove existing password reset OTP if any
        otpVerificationRepository.deleteByEmailAndPurpose(email, OtpPurpose.PASSWORD_RESET);

        // Save new OTP with PASSWORD_RESET purpose
        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(900))
                .purpose(OtpPurpose.PASSWORD_RESET)
                .build();
        otpVerificationRepository.save(otpVerification);

        // Publish to RabbitMQ for email delivery
        PasswordResetEvent event = new PasswordResetEvent(email, otp);
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.PASSWORD_RESET_ROUTING_KEY, event);

        return "If this email is registered, you will receive a password reset code.";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String resetPassword(String email, String otp, String newPassword) {
        OtpVerification otpVerification = otpVerificationRepository
                .findByEmailAndOtpAndPurpose(email, otp, OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset code."));

        if (otpVerification.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Reset code has expired. Please request a new one.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark as used and clean up
        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);
        otpVerificationRepository.deleteByEmailAndPurpose(email, OtpPurpose.PASSWORD_RESET);

        return "Password reset successfully. You can now log in with your new password.";
    }

    @Override
    public String changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password changed successfully.";
    }
}
