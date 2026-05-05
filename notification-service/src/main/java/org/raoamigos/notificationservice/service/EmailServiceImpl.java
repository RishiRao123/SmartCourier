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


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${smartcourier.tracking.url:http://localhost:5173/track/}")
    private String trackingBaseUrl;

    private static final String BRAND_NAME = "SmartCourier";


    // 1. OTP VERIFICATION EMAIL
    @Override
    public void sendOtpEmail(OtpEmailEvent event) {
        String subject = "Verify Your Email — " + BRAND_NAME;
        String html = buildBaseTemplate(
            "Verify Your Email",
            "👋 Hello,",
            "<p style=\"color:#475569;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "Welcome to SmartCourier! Please use the verification code below to confirm your email address.</p>"
            + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:20px;padding:40px;text-align:center;margin:0 0 30px 0;\">"
                + "<p style=\"color:#64748b;font-size:12px;font-weight:800;letter-spacing:3px;text-transform:uppercase;margin:0 0 16px 0;\">YOUR VERIFICATION CODE</p>"
                + "<div style=\"font-size:44px;font-weight:900;letter-spacing:12px;color:#ca8a04;font-family:'Courier New',monospace;\">"
                    + event.getOtp()
                + "</div>"
                + "<p style=\"color:#94a3b8;font-size:13px;margin:16px 0 0 0;\">This code expires in 10 minutes</p>"
            + "</div>"
            + "<p style=\"color:#64748b;font-size:14px;line-height:1.6;margin:0;\">"
                + "If you didn't create an account with SmartCourier, please ignore this email.</p>"
        );
        sendHtmlEmail(event.getEmail(), subject, html);
    }

    // 2. ADMIN CREDENTIALS EMAIL
    @Override
    public void sendAdminCredentialsEmail(AdminCredentialsEvent event) {
        String subject = "Your Admin Account — " + BRAND_NAME;
        String html = buildBaseTemplate(
            "Admin Access Granted",
            "🛡️ Hello " + event.getUsername() + ",",
            "<p style=\"color:#475569;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "A Super Administrator has created an admin account for you on the SmartCourier platform. "
                + "Please use the credentials below to log in.</p>"
            + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:20px;padding:32px;margin:0 0 30px 0;\">"
                + "<table style=\"width:100%;border-collapse:collapse;\">"
                    + "<tr>"
                        + "<td style=\"padding:12px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;border-bottom:1px solid #e2e8f0;\">EMAIL</td>"
                        + "<td style=\"padding:12px 0;color:#0f172a;font-size:16px;font-weight:700;text-align:right;border-bottom:1px solid #e2e8f0;\">" + event.getEmail() + "</td>"
                    + "</tr>"
                    + "<tr>"
                        + "<td style=\"padding:12px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;\">PASSWORD</td>"
                        + "<td style=\"padding:12px 0;color:#ca8a04;font-size:18px;font-weight:900;font-family:'Courier New',monospace;letter-spacing:2px;text-align:right;\">" + event.getRawPassword() + "</td>"
                    + "</tr>"
                + "</table>"
            + "</div>"
            + "<div style=\"background:#fefce8;border-left:4px solid #EAB308;border-radius:0 12px 12px 0;padding:16px 20px;margin:0 0 20px 0;\">"
                + "<p style=\"color:#92400e;font-size:14px;font-weight:700;margin:0;\">⚠️ Please change your password after your first login for security.</p>"
            + "</div>"
        );
        sendHtmlEmail(event.getEmail(), subject, html);
    }

    // 3. DELIVERY BOOKED EMAIL
    @Override
    public void sendDeliveryBookedEmail(DeliveryBookedEvent event) {
        if (event == null || event.getCustomerEmail() == null) {
            log.warn("⚠️ DeliveryBookedEvent is null or missing customerEmail — skipping email");
            return;
        }
        String subject = "Shipment Booked — " + safeStr(event.getTrackingNumber()) + " | " + BRAND_NAME;
        String trackingUrl = trackingBaseUrl + safeStr(event.getTrackingNumber());
        String paymentStr = event.getPaymentMethod() != null ? event.getPaymentMethod().replace("_", " ") : "N/A";
        String priceStr = event.getPrice() != null ? String.format("%.2f", event.getPrice()) : "0.00";
        String html = buildBaseTemplate(
            "Shipment Confirmed",
            "📦 Hello " + safeStr(event.getCustomerName()) + ",",
            "<p style=\"color:#475569;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "Your shipment has been booked successfully! Here are your shipment details:</p>"
            + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:20px;padding:32px;margin:0 0 30px 0;\">"
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

    // 4. DELIVERY DELIVERED EMAIL
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
            "<p style=\"color:#475569;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "Great news! Your package has been delivered successfully.</p>"
            + "<div style=\"background:#f0fdf4;border:1px solid #bbf7d0;border-radius:20px;padding:40px;text-align:center;margin:0 0 30px 0;\">"
                + "<div style=\"font-size:56px;margin:0 0 12px 0;\">✅</div>"
                + "<p style=\"color:#16a34a;font-size:24px;font-weight:900;margin:0 0 8px 0;\">DELIVERED</p>"
                + "<p style=\"color:#22c55e;font-size:14px;font-weight:600;margin:0;\">" + event.getTrackingNumber() + "</p>"
            + "</div>"
            + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:20px;padding:32px;margin:0 0 30px 0;\">"
                + "<table style=\"width:100%;border-collapse:collapse;\">"
                    + buildInfoRow("RECIPIENT", event.getReceiverName(), false)
                    + "<tr>"
                        + "<td style=\"padding:14px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;\">NOTE</td>"
                        + "<td style=\"padding:14px 0;color:#0f172a;font-size:15px;font-weight:600;text-align:right;font-style:italic;\">\"" + note + "\"</td>"
                    + "</tr>"
                + "</table>"
            + "</div>"
            + "<p style=\"color:#64748b;font-size:14px;line-height:1.6;text-align:center;margin:0;\">"
                + "Thank you for choosing SmartCourier! 💛</p>"
        );
        sendHtmlEmail(event.getCustomerEmail(), subject, html);
    }

    @Override
    public void sendDeliveryStatusUpdateEmail(DeliveryStatusUpdateEvent event) {
        if (event == null || event.getCustomerEmail() == null) {
            log.warn("⚠️ DeliveryStatusUpdateEvent is null or missing customerEmail — skipping email");
            return;
        }
        String subject = "Status Update: " + safeStr(event.getNewStatus()) + " — " + BRAND_NAME;
        String trackingUrl = trackingBaseUrl + safeStr(event.getTrackingNumber());
        String html = buildBaseTemplate(
            "Shipment Update",
            "🚚 Hello " + safeStr(event.getCustomerName()) + ",",
            "<p style=\"color:#475569;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "Your shipment has a new status update.</p>"
            + "<div style=\"background:#eff6ff;border:1px solid #bfdbfe;border-radius:20px;padding:40px;text-align:center;margin:0 0 30px 0;\">"
                + "<p style=\"color:#64748b;font-size:12px;font-weight:800;letter-spacing:3px;text-transform:uppercase;margin:0 0 16px 0;\">CURRENT STATUS</p>"
                + "<div style=\"font-size:32px;font-weight:900;color:#2563eb;letter-spacing:-1px;\">"
                    + safeStr(event.getNewStatus()).replace("_", " ")
                + "</div>"
                + (event.getLocation() != null ? "<p style=\"color:#3b82f6;font-size:14px;font-weight:600;margin:12px 0 0 0;\">📍 " + event.getLocation() + "</p>" : "")
            + "</div>"
            + "<div style=\"text-align:center;margin:0 0 30px 0;\">"
                + "<a href=\"" + trackingUrl + "\" style=\"display:inline-block;background:#EAB308;color:#071a2a;font-weight:900;font-size:16px;padding:16px 40px;border-radius:14px;text-decoration:none;letter-spacing:1px;\">TRACK SHIPMENT →</a>"
            + "</div>"
        );
        sendHtmlEmail(event.getCustomerEmail(), subject, html);
    }


    // PRIVATE HELPERS

    // Builds a table row for the info panels in email templates.
    // Null-safe string helper — returns empty string if value is null */
    private String safeStr(String value) {
        return value != null ? value : "";
    }

    private String buildInfoRow(String label, String value, boolean highlight) {
        String valueColor = highlight ? "#ca8a04" : "#0f172a";
        String valueWeight = highlight ? "900" : "600";
        String valueSize = highlight ? "18px" : "15px";
        return "<tr>"
            + "<td style=\"padding:14px 0;color:#64748b;font-size:12px;font-weight:800;letter-spacing:2px;text-transform:uppercase;border-bottom:1px solid #e2e8f0;\">" + label + "</td>"
            + "<td style=\"padding:14px 0;color:" + valueColor + ";font-size:" + valueSize + ";font-weight:" + valueWeight + ";text-align:right;border-bottom:1px solid #e2e8f0;\">" + value + "</td>"
            + "</tr>";
    }

    
    private String buildBaseTemplate(String headerTitle, String greeting, String bodyContent) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head>"
            + "<body style=\"margin:0;padding:0;background-color:#f4f7fa;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">"
            + "<div style=\"max-width:600px;margin:0 auto;padding:60px 20px;\">"

                // Logo Header
                + "<div style=\"text-align:center;margin:0 0 40px 0;\">"
                    + "<div style=\"display:inline-block;background:#EAB308;width:64px;height:64px;border-radius:20px;line-height:64px;text-align:center;font-size:32px;margin:0 0 20px 0;box-shadow:0 12px 24px rgba(234,179,8,0.4);\">📦</div>"
                    + "<h1 style=\"color:#071a2a;font-size:32px;font-weight:900;margin:0;letter-spacing:-1px;\">Smart<span style=\"color:#EAB308;\">Courier</span></h1>"
                + "</div>"

                // Main Card
                + "<div style=\"background:#ffffff;border-radius:32px;padding:56px 48px;border:1px solid #eef2f6;box-shadow:0 20px 40px -10px rgba(7,26,42,0.1);\">"

                    // Header badge
                    + "<div style=\"text-align:center;margin:0 0 40px 0;\">"
                        + "<span style=\"display:inline-block;background:rgba(234,179,8,0.1);color:#ca8a04;font-size:12px;font-weight:900;letter-spacing:4px;text-transform:uppercase;padding:10px 24px;border-radius:100px;border:1px solid rgba(234,179,8,0.2);\">"
                            + headerTitle
                        + "</span>"
                    + "</div>"

                    // Greeting
                    + "<h2 style=\"color:#0f172a;font-size:26px;font-weight:900;margin:0 0 24px 0;letter-spacing:-0.5px;\">" + greeting + "</h2>"

                    // Dynamic body content
                    + bodyContent

                + "</div>"

                // Footer
                + "<div style=\"text-align:center;margin:48px 0 0 0;\">"
                    + "<div style=\"margin:0 0 24px 0;\">"
                        + "<span style=\"color:#071a2a;font-weight:900;font-size:14px;\">Smart</span><span style=\"color:#EAB308;font-weight:900;font-size:14px;\">Courier</span>"
                    + "</div>"
                    + "<p style=\"color:#64748b;font-size:13px;margin:0 0 10px 0;font-weight:600;\">Premium Logistics. Global Reach. Local Care.</p>"
                    + "<p style=\"color:#94a3b8;font-size:11px;margin:0;line-height:1.6;\">"
                        + "© 2026 SmartCourier International. All rights reserved.<br/>"
                        + "This is an automated security notification. Do not reply."
                    + "</p>"
                + "</div>"

            + "</div>"
            + "</body></html>";
    }


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


    // 5. PASSWORD RESET EMAIL
    @Override
    public void sendPasswordResetEmail(PasswordResetEvent event) {
        if (event == null || event.getEmail() == null) {
            log.warn("⚠️ PasswordResetEvent is null or missing email — skipping email");
            return;
        }
        String subject = "Reset Your Password — " + BRAND_NAME;
        String html = buildBaseTemplate(
            "Reset Your Password",
            "🔒 Hello,",
            "<p style=\"color:#475569;font-size:16px;line-height:1.7;margin:0 0 30px 0;\">"
                + "We received a request to reset your password. Use the code below to set a new password.</p>"
            + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:20px;padding:40px;text-align:center;margin:0 0 30px 0;\">"
                + "<p style=\"color:#64748b;font-size:12px;font-weight:800;letter-spacing:3px;text-transform:uppercase;margin:0 0 16px 0;\">YOUR RESET CODE</p>"
                + "<div style=\"font-size:44px;font-weight:900;letter-spacing:12px;color:#ca8a04;font-family:'Courier New',monospace;\">"
                    + safeStr(event.getOtp())
                + "</div>"
                + "<p style=\"color:#94a3b8;font-size:13px;margin:16px 0 0 0;\">This code expires in 15 minutes</p>"
            + "</div>"
            + "<p style=\"color:#ef4444;font-size:14px;line-height:1.6;margin:0 0 20px 0;font-weight:600;\">"
                + "⚠️ If you did not request a password reset, please ignore this email. Your password will remain unchanged.</p>"
            + "<p style=\"color:#64748b;font-size:13px;line-height:1.6;margin:0;\">"
                + "For security, never share this code with anyone. SmartCourier staff will never ask for it.</p>"
        );
        sendHtmlEmail(event.getEmail(), subject, html);
    }
}
