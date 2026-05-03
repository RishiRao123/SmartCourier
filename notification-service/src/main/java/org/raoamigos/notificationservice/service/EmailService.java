package org.raoamigos.notificationservice.service;

import org.raoamigos.notificationservice.dto.*;

/**
 * Core email service interface for all SmartCourier notification types.
 */
public interface EmailService {

    void sendOtpEmail(OtpEmailEvent event);

    void sendAdminCredentialsEmail(AdminCredentialsEvent event);

    void sendDeliveryBookedEmail(DeliveryBookedEvent event);

    void sendDeliveryDeliveredEmail(DeliveryDeliveredEvent event);

    void sendPasswordResetEmail(PasswordResetEvent event);
}
