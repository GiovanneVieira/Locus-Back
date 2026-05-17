package com.project.locusapi.service;

import com.project.locusapi.service.communication.CommunicationManager;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailService implements CommunicationManager {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendMsg(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("Locus");
        mailSender.send(message);
    }

    public void sendWelcomeEmail(String to, String subject) {

        Map<String, Object> variables = Map.of("username", to);

        try {
            emailHelper(to, subject, "welcome/html", variables);
        } catch (Exception e) {
            throw new RuntimeException("Error rendering or sending email template", e);
        }
    }

    public void sendOTPEmail(String to, String otp, String username) {
        Map<String, Object> variables = Map.ofEntries(Map.entry("username", username), Map.entry("otpCode", otp));
        try {
            emailHelper(to, "Verifique sua conta - Locus", "emails/otp", variables);
        }catch (Exception e){
            throw new RuntimeException("Error rendering or sending email template", e);
        }
    }

    private void emailHelper(String to, String subject, String htmlPath, Map<String, Object> variables) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }

            String htmlContent = templateEngine.process(htmlPath, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("Locus <no-reply@locusapp.com>");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error rendering or sending email template: " + htmlPath, e);
        }
    }

}
