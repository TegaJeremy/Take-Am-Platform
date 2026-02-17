package com.takeam.userservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String FROM_EMAIL = "noreply@takeam.ng";
    private static final String FROM_NAME = "TakeAm";
    private static final String BRAND_COLOR = "#2E7D32";
    private static final String BRAND_LIGHT = "#E8F5E9";

    // ─────────────────────────────────────────────
    // OTP EMAILS
    // ─────────────────────────────────────────────

    public void sendOTPEmail(String toEmail, String otp, String recipientName) {
        String subject = "TakeAm - Your Verification Code";
        String html = buildOtpHtml(recipientName, otp, "verify your email address");
        sendHtmlEmail(toEmail, subject, html);
    }

    public void sendLoginOTPEmail(String toEmail, String otp, String recipientName) {
        String subject = "TakeAm - Login Verification Code";
        String html = buildOtpHtml(recipientName, otp, "complete your login");
        sendHtmlEmail(toEmail, subject, html);
    }

    public void sendPasswordResetEmail(String toEmail, String otp, String recipientName) {
        String subject = "TakeAm - Password Reset Code";
        String html = buildOtpHtml(recipientName, otp, "reset your password");
        sendHtmlEmail(toEmail, subject, html);
    }

    // ─────────────────────────────────────────────
    // WELCOME EMAILS
    // ─────────────────────────────────────────────

    public void sendWelcomeEmail(String toEmail, String recipientName, String role) {
        String subject = "Welcome to TakeAm! 🎉";
        String html = buildWelcomeHtml(recipientName, role);
        sendHtmlEmail(toEmail, subject, html);
    }

    // ─────────────────────────────────────────────
    // ACCOUNT STATUS EMAILS
    // ─────────────────────────────────────────────

    public void sendAccountApprovedEmail(String toEmail, String recipientName) {
        String subject = "TakeAm - Your Account Has Been Approved!";
        String html = buildAccountApprovedHtml(recipientName);
        sendHtmlEmail(toEmail, subject, html);
    }

    public void sendAccountLockedEmail(String toEmail, String recipientName) {
        String subject = "TakeAm - Account Temporarily Locked";
        String html = buildAccountLockedHtml(recipientName);
        sendHtmlEmail(toEmail, subject, html);
    }

    public void sendPasswordChangedEmail(String toEmail, String recipientName) {
        String subject = "TakeAm - Password Changed Successfully";
        String html = buildPasswordChangedHtml(recipientName);
        sendHtmlEmail(toEmail, subject, html);
    }

    // ─────────────────────────────────────────────
    // CORE SEND METHOD
    // ─────────────────────────────────────────────

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to: {} | Subject: {}", toEmail, subject);

        } catch (Exception e) {
            log.error("Failed to send email to: {} | Subject: {} | Error: {}", toEmail, subject, e.getMessage());
            log.info("=== EMAIL FALLBACK === To: {} | Subject: {}", toEmail, subject);
        }
    }

    // ─────────────────────────────────────────────
    // HTML TEMPLATES
    // ─────────────────────────────────────────────

    private String buildOtpHtml(String name, String otp, String purpose) {
        return baseTemplate(String.format("""
            <h2 style="color:#2E7D32;margin:0 0 16px;">Verification Code</h2>
            <p style="color:#555;font-size:15px;">Hi <strong>%s</strong>,</p>
            <p style="color:#555;font-size:15px;">Use the code below to %s:</p>
            
            <div style="background:#E8F5E9;border-radius:12px;padding:24px;text-align:center;margin:24px 0;">
                <span style="font-size:42px;font-weight:bold;letter-spacing:8px;color:#2E7D32;">%s</span>
            </div>
            
            <p style="color:#888;font-size:13px;">⏱ This code expires in <strong>5 minutes</strong>.</p>
            <p style="color:#888;font-size:13px;">🔒 Never share this code with anyone.</p>
            <p style="color:#888;font-size:13px;">If you didn't request this, you can safely ignore this email.</p>
            """, name, purpose, otp));
    }

    private String buildWelcomeHtml(String name, String role) {
        String roleMessage = switch (role.toUpperCase()) {
            case "TRADER" -> "You can now receive payments, manage your stall, and grow your business digitally.";
            case "AGENT" -> "You can now register traders and help grow the TakeAm marketplace in your area.";
            case "BUYER" -> "You can now browse fresh produce, place orders, and enjoy fast delivery.";
            default -> "You now have access to the TakeAm marketplace.";
        };

        return baseTemplate(String.format("""
            <h2 style="color:#2E7D32;margin:0 0 16px;">Welcome to TakeAm! 🎉</h2>
            <p style="color:#555;font-size:15px;">Hi <strong>%s</strong>,</p>
            <p style="color:#555;font-size:15px;">Your account has been successfully verified. %s</p>
            
            <div style="background:#E8F5E9;border-radius:12px;padding:20px;margin:24px 0;border-left:4px solid #2E7D32;">
                <p style="margin:0;color:#2E7D32;font-weight:bold;">🌿 Fresh produce. Fair prices. Fast delivery.</p>
                <p style="margin:8px 0 0;color:#555;font-size:14px;">TakeAm connects markets, traders, and buyers across Nigeria.</p>
            </div>
            
            <p style="color:#555;font-size:15px;">If you have any questions, reply to this email or contact our support team.</p>
            """, name, roleMessage));
    }

    private String buildAccountApprovedHtml(String name) {
        return baseTemplate(String.format("""
            <h2 style="color:#2E7D32;margin:0 0 16px;">Account Approved! ✅</h2>
            <p style="color:#555;font-size:15px;">Hi <strong>%s</strong>,</p>
            <p style="color:#555;font-size:15px;">Great news! Your TakeAm agent account has been reviewed and <strong>approved</strong>.</p>
            
            <div style="background:#E8F5E9;border-radius:12px;padding:20px;margin:24px 0;border-left:4px solid #2E7D32;">
                <p style="margin:0;color:#2E7D32;font-weight:bold;">You can now:</p>
                <ul style="color:#555;font-size:14px;margin:8px 0 0;padding-left:20px;">
                    <li>Log in to your agent dashboard</li>
                    <li>Register traders in your area</li>
                    <li>Track your registered traders</li>
                    <li>Record agent attendance</li>
                </ul>
            </div>
            
            <p style="color:#555;font-size:15px;">Log in now to get started. Welcome to the team! 🚀</p>
            """, name));
    }

    private String buildAccountLockedHtml(String name) {
        return baseTemplate(String.format("""
            <h2 style="color:#c0392b;margin:0 0 16px;">Account Temporarily Locked 🔒</h2>
            <p style="color:#555;font-size:15px;">Hi <strong>%s</strong>,</p>
            <p style="color:#555;font-size:15px;">Your TakeAm account has been temporarily locked due to multiple failed login attempts.</p>
            
            <div style="background:#FFEBEE;border-radius:12px;padding:20px;margin:24px 0;border-left:4px solid #c0392b;">
                <p style="margin:0;color:#c0392b;font-weight:bold;">⏱ Your account will be unlocked in 30 minutes.</p>
                <p style="margin:8px 0 0;color:#555;font-size:14px;">If this wasn't you, please contact support immediately.</p>
            </div>
            
            <p style="color:#555;font-size:15px;">For help, reply to this email or contact our support team.</p>
            """, name));
    }

    private String buildPasswordChangedHtml(String name) {
        return baseTemplate(String.format("""
            <h2 style="color:#2E7D32;margin:0 0 16px;">Password Changed ✅</h2>
            <p style="color:#555;font-size:15px;">Hi <strong>%s</strong>,</p>
            <p style="color:#555;font-size:15px;">Your TakeAm account password has been successfully changed.</p>
            
            <div style="background:#FFF3E0;border-radius:12px;padding:20px;margin:24px 0;border-left:4px solid #FF9800;">
                <p style="margin:0;color:#E65100;font-weight:bold;">⚠️ Wasn't you?</p>
                <p style="margin:8px 0 0;color:#555;font-size:14px;">If you didn't make this change, contact our support team immediately to secure your account.</p>
            </div>
            
            <p style="color:#555;font-size:15px;">Your account security is our priority. Stay safe!</p>
            """, name));
    }

    // ─────────────────────────────────────────────
    // BASE TEMPLATE
    // ─────────────────────────────────────────────

    private String baseTemplate(String content) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                    <tr><td align="center">
                        <table width="580" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                            
                            <!-- HEADER -->
                            <tr>
                                <td style="background:#2E7D32;padding:28px 40px;text-align:center;">
                                    <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:800;letter-spacing:1px;">🌿 TakeAm</h1>
                                    <p style="margin:4px 0 0;color:#A5D6A7;font-size:13px;">Fresh Markets. Digital Future.</p>
                                </td>
                            </tr>
                            
                            <!-- CONTENT -->
                            <tr>
                                <td style="padding:40px;">
                                    %s
                                </td>
                            </tr>
                            
                            <!-- FOOTER -->
                            <tr>
                                <td style="background:#f9f9f9;padding:24px 40px;text-align:center;border-top:1px solid #eee;">
                                    <p style="margin:0;color:#aaa;font-size:12px;">© 2026 TakeAm. All rights reserved.</p>
                                    <p style="margin:4px 0 0;color:#aaa;font-size:12px;">This is an automated message, please do not reply directly.</p>
                                </td>
                            </tr>
                            
                        </table>
                    </td></tr>
                </table>
            </body>
            </html>
            """, content);
    }
}