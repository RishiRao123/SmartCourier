package org.raoamigos.authservice.repository;

import org.raoamigos.authservice.entity.Role;
import org.raoamigos.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleIn(List<Role> roles);

    boolean existsByEmail(String email);

    long countByRole(Role role);
}
