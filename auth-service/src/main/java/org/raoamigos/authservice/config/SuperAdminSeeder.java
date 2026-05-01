package org.raoamigos.authservice.config;

import lombok.RequiredArgsConstructor;
import org.raoamigos.authservice.entity.Role;
import org.raoamigos.authservice.entity.User;
import org.raoamigos.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String superAdminEmail = "risxi.rao@gmail.com";

        if (userRepository.findByEmail(superAdminEmail).isEmpty()) {
            User superAdmin = User.builder()
                    .username("Rishi rao")
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode("Ri$hi&61416@"))
                    .role(Role.ROLE_SUPER_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(superAdmin);
            System.out.println("✅ Super Admin seeded successfully: " + superAdminEmail);
        } else {
            System.out.println("ℹ️ Super Admin already exists: " + superAdminEmail);
        }
    }
}
