package com.mentorhub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * EmailService - sends transactional email via SMTP.
 *
 * CONFIGURATION:
 * Reads spring.mail.host/port/username/password, which in turn come from
 * the SMTP_HOST / SMTP_PORT / SMTP_USERNAME / SMTP_PASSWORD environment
 * variables (see application.properties) — never hardcoded here. Any
 * standard SMTP provider works (Gmail with an app password, SendGrid's SMTP
 * relay, Mailgun, etc.); this class doesn't care which one you use.
 *
 * LOCAL DEV FALLBACK:
 * If SMTP_HOST is unset (e.g. a laptop with no mail credentials configured),
 * we don't want registration to 500 just because nobody set up email yet.
 * In that case we log the link at INFO level instead of sending, so the
 * verification flow is still testable end-to-end locally. Once SMTP_HOST is
 * set, real email is sent and this fallback never triggers.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendVerificationEmail(String toEmail, String name, String verificationLink) {
        String subject = "Verify your MentR email address";
        String body = "Hi " + name + ",\n\n"
                + "Please verify your email address by clicking the link below. "
                + "This link expires in 24 hours and can only be used once.\n\n"
                + verificationLink + "\n\n"
                + "If you didn't create a MentR account, you can ignore this email.\n\n"
                + "- MentR";

        if (mailSender == null || mailHost == null || mailHost.isBlank()) {
            log.info("[EmailService] SMTP is not configured (SMTP_HOST unset) — logging the " +
                    "verification link instead of emailing it. Set SMTP_HOST/SMTP_USERNAME/" +
                    "SMTP_PASSWORD to send real email. Link for {}: {}", toEmail, verificationLink);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
