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

    public void sendWelcomeEmail(String to, String subject, String username) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Passa variáveis do Java direto para as tags do HTML
            Context context = new Context();
            context.setVariable("username", username);

            // Processa o arquivo HTML localizado na pasta resources/templates/emails/
            String htmlContent = templateEngine.process("emails/welcome", context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("Locus <no-reply@locusapp.com>");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error rendering or sending email template", e);
        }
    }


}
