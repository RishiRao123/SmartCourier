package org.raoamigos.authservice.service;

import org.raoamigos.authservice.dto.LoginRequestDTO;
import org.raoamigos.authservice.dto.RegisterRequestDTO;


public interface AuthService {

    String register(RegisterRequestDTO registerRequestDTO);
    String login(LoginRequestDTO loginRequestDTO);
    String registerAdmin(RegisterRequestDTO registerRequestDTO);
}
