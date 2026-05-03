package org.raoamigos.authservice.service;

import org.raoamigos.authservice.dto.LoginRequestDTO;
import org.raoamigos.authservice.dto.RegisterRequestDTO;
import org.raoamigos.authservice.entity.User;

public interface AuthService {

    String register(RegisterRequestDTO registerRequestDTO);
    String login(LoginRequestDTO loginRequestDTO);
    String registerAdmin(RegisterRequestDTO registerRequestDTO);
    
    String verifyOtp(String email, String otp);
    
    String resendOtp(String email);

    User getUserProfile(String email);

    // Password Management
    String forgotPassword(String email);
    String resetPassword(String email, String otp, String newPassword);
    String changePassword(Long userId, String oldPassword, String newPassword);
}
