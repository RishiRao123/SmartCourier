package org.raoamigos.authservice.repository;

import org.raoamigos.authservice.entity.OtpVerification;
import org.raoamigos.authservice.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmailAndOtp(String email, String otp);
    Optional<OtpVerification> findByEmail(String email);
    void deleteByEmail(String email);

    // Purpose-aware queries
    Optional<OtpVerification> findByEmailAndOtpAndPurpose(String email, String otp, OtpPurpose purpose);
    Optional<OtpVerification> findByEmailAndPurpose(String email, OtpPurpose purpose);
    void deleteByEmailAndPurpose(String email, OtpPurpose purpose);
}
