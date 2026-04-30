package org.raoamigos.authservice.service.impl;

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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequestDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.ROLE_CUSTOMER)
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }

    public String login(LoginRequestDTO dto) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getEmail(),
                dto.getPassword()
        ));

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Include username in the JWT for frontend display
        return "token: " + jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(), user.getUsername());
    }

    @Override
    public String registerAdmin(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.ROLE_ADMIN)
                .build();

        userRepository.save(user);
        return "Admin registered successfully";
    }

}
