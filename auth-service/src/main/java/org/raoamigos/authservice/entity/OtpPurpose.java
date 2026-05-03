package org.raoamigos.authservice.entity;

/**
 * Distinguishes the purpose of an OTP record in the otp_verification table.
 */
public enum OtpPurpose {
    SIGNUP_OTP,
    PASSWORD_RESET
}
