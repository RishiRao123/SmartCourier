package org.raoamigos.authservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.authservice.dto.LoginRequestDTO;
import org.raoamigos.authservice.dto.RegisterRequestDTO;
import org.raoamigos.authservice.entity.Role;
import org.raoamigos.authservice.entity.User;
import org.raoamigos.authservice.repository.UserRepository;
import org.raoamigos.authservice.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerDto;
    private LoginRequestDTO loginDto;
    private User dummyUser;

    @BeforeEach
    void setUp() {
        registerDto = new RegisterRequestDTO();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("password123");

        loginDto = new LoginRequestDTO();
        loginDto.setEmail("test@test.com");
        loginDto.setPassword("password123");

        dummyUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded_password")
                .role(Role.ROLE_CUSTOMER)
                .build();
    }

    @Test
    void register_ShouldSaveUser_WhenEmailIsUnique() {
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encoded_password");

        String result = authService.register(registerDto);

        assertEquals("User registered successfully", result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(Role.ROLE_CUSTOMER, savedUser.getRole());
        assertEquals("encoded_password", savedUser.getPassword());
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.of(dummyUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.register(registerDto)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(dummyUser));
        when(jwtUtil.generateToken(dummyUser.getEmail(), dummyUser.getId(), dummyUser.getRole().name()))
                .thenReturn("dummy.jwt.token");

        String result = authService.login(loginDto);

        assertEquals("token: dummy.jwt.token", result);
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(loginDto)
        );

        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }
}