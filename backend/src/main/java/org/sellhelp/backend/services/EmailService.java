package org.sellhelp.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.sellhelp.backend.security.CurrentUser;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final JWTUtil jwtUtil;
    private final TemplateEngine templateEngine;
    private final CurrentUser currentUser;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, JWTUtil jwtUtil,
                        TemplateEngine templateEngine, CurrentUser currentUser){
        this.javaMailSender = javaMailSender;
        this.jwtUtil = jwtUtil;
        this.templateEngine = templateEngine;
        this.currentUser = currentUser;
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text); // plain text
        message.setFrom(fromEmail); // sender email

        javaMailSender.send(message);
    }

    public void registrationSuccessEmail(String email)
    {
        sendSimpleEmail(email, "Sikeres regisztráció", "Tisztelt Felhasználó!\nSikeresen regisztrált a SellHelp platformra!");
    }


    // TODO: MessagingException needs global exception handler
    public void sendHTMLTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) throws MessagingException {
        Context context = new Context();
        context.setVariables(variables);

        String html = templateEngine.process(templateName, context);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        javaMailSender.send(message);
    }

    public void updatePassword()
    {
        try {
            String toEmail = currentUser.getCurrentlyLoggedUserEmail();
            Map<String, Object> variables = new HashMap<>();
            String token = jwtUtil.generatePasswordUpdateToken(toEmail);

            String resetLink =
                    "http://localhost:3000/reset-password?token=" + token;
            variables.put("resetLink", resetLink);

            sendHTMLTemplateEmail("metzroland1111@gmail.com", "Jelszó módosítás", "emails/passwordUpdate", variables);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
