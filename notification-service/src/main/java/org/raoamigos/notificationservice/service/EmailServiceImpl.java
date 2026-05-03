package org.raoamigos.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.notificationservice.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService that sends branded HTML emails via Gmail SMTP.
 * All emails use the SmartCourier blue/yellow brand identity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String BRAND_NAME = "SmartCourier";
    private static final String TRACKING_BASE_URL = "http://localhost:5173/track/";

    // =============================================
    // 1. OTP VERIFICATION EMAIL
    // =============================================
    @Override
    public void sendOtpEmail(OtpEmailEvent event) {
        String subject = "Verify Your Email — " + BRAND_NAME;
        String html = buildBaseTemplate(
            "Verify Your Email",
            "👋 Hello,",
            "<p style=\"color:#94a3b8;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "Welcome to SmartCourier! Please use the verification code below to confirm your email address.</p>"
            + "<div style=\"background:linear-gradient(135deg,#0f2942 0%,#1a3a5c 100%);border-radius:20px;padding:40px;text-align:center;margin:0 0 30px 0;\">"
                + "<p style=\"color:#94a3b8;font-size:12px;font-weight:800;letter-spacing:3px;text-transform:uppercase;margin:0 0 16px 0;\">YOUR VERIFICATION CODE</p>"
                + "<div style=\"font-size:44px;font-weight:900;letter-spacing:12px;color:#EAB308;font-family:'Courier New',monospace;\">"
                    + event.getOtp()
                + "</div>"
                + "<p style=\"color:#64748b;font-size:13px;margin:16px 0 0 0;\">This code expires in 10 minutes</p>"
            + "</div>"
            + "<p style=\"color:#64748b;font-size:14px;line-height:1.6;margin:0;\">"
                + "If you didn't create an account with SmartCourier, please ignore this email.</p>"
        );
        sendHtmlEmail(event.getEmail(), subject, html);
    }

    // =============================================
    // 2. ADMIN CREDENTIALS EMAIL
    // =============================================
    @Override
    public void sendAdminCredentialsEmail(AdminCredentialsEvent event) {
        String subject = "Your Admin Account — " + BRAND_NAME;
        String html = buildBaseTemplate(
            "Admin Access Granted",
            "🛡️ Hello " + event.getUsername() + ",",
            "<p style=\"color:#94a3b8;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "A Super Administrator has created an admin account for you on the SmartCourier platform. "
                + "Please use the credentials below to log in.</p>"
            + "<div style=\"background:linear-gradient(135deg,#0f2942 0%,#1a3a5c 100%);border-radius:20px;padding:32px;margin:0 0 30px 0;\">"
                + "<table style=\"width:100%;border-collapse:collapse;\">"
                    + "<tr>"
                        + "<td style=\"padding:12px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;border-bottom:1px solid #1e3a5f;\">EMAIL</td>"
                        + "<td style=\"padding:12px 0;color:#e2e8f0;font-size:16px;font-weight:700;text-align:right;border-bottom:1px solid #1e3a5f;\">" + event.getEmail() + "</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td style=\"padding:12px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;\">PASSWORD</td>"
                        + "<td style=\"padding:12px 0;color:#EAB308;font-size:18px;font-weight:900;font-family:'Courier New',monospace;letter-spacing:2px;text-align:right;\">" + event.getRawPassword() + "</td>"
                    + "</tr>"
                + "</table>"
            + "</div>"
            + "<div style=\"background:#fefce8;border-left:4px solid #EAB308;border-radius:0 12px 12px 0;padding:16px 20px;margin:0 0 20px 0;\">"
                + "<p style=\"color:#92400e;font-size:14px;font-weight:700;margin:0;\">⚠️ Please change your password after your first login for security.</p>"
            + "</div>"
        );
        sendHtmlEmail(event.getEmail(), subject, html);
    }

    // =============================================
    // 3. DELIVERY BOOKED EMAIL
    // =============================================
    @Override
    public void sendDeliveryBookedEmail(DeliveryBookedEvent event) {
        if (event == null || event.getCustomerEmail() == null) {
            log.warn("⚠️ DeliveryBookedEvent is null or missing customerEmail — skipping email");
            return;
        }
        String subject = "Shipment Booked — " + safeStr(event.getTrackingNumber()) + " | " + BRAND_NAME;
        String trackingUrl = TRACKING_BASE_URL + safeStr(event.getTrackingNumber());
        String paymentStr = event.getPaymentMethod() != null ? event.getPaymentMethod().replace("_", " ") : "N/A";
        String priceStr = event.getPrice() != null ? String.format("%.2f", event.getPrice()) : "0.00";
        String html = buildBaseTemplate(
            "Shipment Confirmed",
            "📦 Hello " + safeStr(event.getCustomerName()) + ",",
            "<p style=\"color:#94a3b8;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "Your shipment has been booked successfully! Here are your shipment details:</p>"
            + "<div style=\"background:linear-gradient(135deg,#0f2942 0%,#1a3a5c 100%);border-radius:20px;padding:32px;margin:0 0 30px 0;\">"
                + "<table style=\"width:100%;border-collapse:collapse;\">"
                    + buildInfoRow("TRACKING ID", safeStr(event.getTrackingNumber()), true)
                    + buildInfoRow("RECEIVER", safeStr(event.getReceiverName()), false)
                    + buildInfoRow("DESTINATION", safeStr(event.getReceiverCity()), false)
                    + buildInfoRow("PAYMENT", paymentStr, false)
                    + "<tr>"
                        + "<td style=\"padding:14px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;\">AMOUNT</td>"
                        + "<td style=\"padding:14px 0;color:#22c55e;font-size:22px;font-weight:900;text-align:right;\">₹" + priceStr + "</td>"
                    + "</tr>"
                + "</table>"
            + "</div>"
            + "<div style=\"text-align:center;margin:0 0 30px 0;\">"
                + "<a href=\"" + trackingUrl + "\" style=\"display:inline-block;background:#EAB308;color:#071a2a;font-weight:900;font-size:16px;padding:16px 40px;border-radius:14px;text-decoration:none;letter-spacing:1px;\">TRACK YOUR SHIPMENT →</a>"
            + "</div>"
            + "<p style=\"color:#64748b;font-size:13px;text-align:center;margin:0;\">"
                + "You can also track your shipment at any time from your dashboard.</p>"
        );
        sendHtmlEmail(event.getCustomerEmail(), subject, html);
    }

    // =============================================
    // 4. DELIVERY DELIVERED EMAIL
    // =============================================
    @Override
    public void sendDeliveryDeliveredEmail(DeliveryDeliveredEvent event) {
        if (event == null || event.getCustomerEmail() == null) {
            log.warn("⚠️ DeliveryDeliveredEvent is null or missing customerEmail — skipping email");
            return;
        }
        String subject = "Delivered! — " + safeStr(event.getTrackingNumber()) + " | " + BRAND_NAME;
        String note = (event.getDeliveryNote() != null && !event.getDeliveryNote().isEmpty())
            ? event.getDeliveryNote()
            : "Package delivered successfully.";
        String html = buildBaseTemplate(
            "Package Delivered",
            "🎉 Hello,",
            "<p style=\"color:#94a3b8;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "Great news! Your package has been delivered successfully.</p>"
            + "<div style=\"background:linear-gradient(135deg,#052e16 0%,#14532d 100%);border-radius:20px;padding:40px;text-align:center;margin:0 0 30px 0;\">"
                + "<div style=\"font-size:56px;margin:0 0 12px 0;\">✅</div>"
                + "<p style=\"color:#22c55e;font-size:24px;font-weight:900;margin:0 0 8px 0;\">DELIVERED</p>"
                + "<p style=\"color:#86efac;font-size:14px;font-weight:600;margin:0;\">" + event.getTrackingNumber() + "</p>"
            + "</div>"
            + "<div style=\"background:linear-gradient(135deg,#0f2942 0%,#1a3a5c 100%);border-radius:20px;padding:32px;margin:0 0 30px 0;\">"
                + "<table style=\"width:100%;border-collapse:collapse;\">"
                    + buildInfoRow("RECIPIENT", event.getReceiverName(), false)
                    + "<tr>"
                        + "<td style=\"padding:14px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;\">NOTE</td>"
                        + "<td style=\"padding:14px 0;color:#e2e8f0;font-size:15px;font-weight:600;text-align:right;font-style:italic;\">\"" + note + "\"</td>"
                    + "</tr>"
                + "</table>"
            + "</div>"
            + "<p style=\"color:#64748b;font-size:14px;line-height:1.6;text-align:center;margin:0;\">"
                + "Thank you for choosing SmartCourier! 💛</p>"
        );
        sendHtmlEmail(event.getCustomerEmail(), subject, html);
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    /**
     * Builds a table row for the info panels in email templates.
     */
    /** Null-safe string helper — returns empty string if value is null */
    private String safeStr(String value) {
        return value != null ? value : "";
    }

    private String buildInfoRow(String label, String value, boolean highlight) {
        String valueColor = highlight ? "#EAB308" : "#e2e8f0";
        String valueWeight = highlight ? "900" : "600";
        String valueSize = highlight ? "18px" : "15px";
        return "<tr>"
            + "<td style=\"padding:14px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;border-bottom:1px solid #1e3a5f;\">" + label + "</td>"
            + "<td style=\"padding:14px 0;color:" + valueColor + ";font-size:" + valueSize + ";font-weight:" + valueWeight + ";text-align:right;border-bottom:1px solid #1e3a5f;\">" + value + "</td>"
            + "</tr>";
    }

    /**
     * Builds the base HTML email wrapper with SmartCourier branding.
     * All 4 email types share this consistent shell.
     */
    private String buildBaseTemplate(String headerTitle, String greeting, String bodyContent) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head>"
            + "<body style=\"margin:0;padding:0;background-color:#0f172a;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;\">"
            + "<div style=\"max-width:600px;margin:0 auto;padding:40px 20px;\">"

                // Logo Header
                + "<div style=\"text-align:center;margin:0 0 32px 0;\">"
                    + "<div style=\"display:inline-block;background:#EAB308;width:52px;height:52px;border-radius:16px;line-height:52px;text-align:center;font-size:24px;margin:0 0 16px 0;\">📦</div>"
                    + "<h1 style=\"color:#ffffff;font-size:28px;font-weight:900;margin:0;\">Smart<span style=\"color:#EAB308;\">Courier</span></h1>"
                + "</div>"

                // Main Card
                + "<div style=\"background:#1e293b;border-radius:24px;padding:48px 40px;border:1px solid #334155;\">"

                    // Header badge
                    + "<div style=\"text-align:center;margin:0 0 32px 0;\">"
                        + "<span style=\"display:inline-block;background:rgba(234,179,8,0.15);color:#EAB308;font-size:11px;font-weight:800;letter-spacing:3px;text-transform:uppercase;padding:8px 20px;border-radius:100px;\">"
                            + headerTitle
                        + "</span>"
                    + "</div>"

                    // Greeting
                    + "<h2 style=\"color:#f1f5f9;font-size:22px;font-weight:800;margin:0 0 20px 0;\">" + greeting + "</h2>"

                    // Dynamic body content
                    + bodyContent

                + "</div>"

                // Footer
                + "<div style=\"text-align:center;margin:32px 0 0 0;\">"
                    + "<p style=\"color:#475569;font-size:12px;margin:0 0 8px 0;\">© 2026 SmartCourier — Your Trusted Logistics Partner</p>"
                    + "<p style=\"color:#334155;font-size:11px;margin:0;\">This is an automated email. Please do not reply directly.</p>"
                + "</div>"

            + "</div>"
            + "</body></html>";
    }

    /**
     * Sends an HTML email via JavaMailSender.
     * Logs success or failure for monitoring.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, BRAND_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("✅ Email sent successfully to: {} | Subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {} | Subject: {} | Error: {}", to, subject, e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email to: {} | Error: {}", to, e.getMessage(), e);
        }
    }
}
