package org.sellhelp.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.sellhelp.backend.exceptions.EmailException;
import org.sellhelp.backend.security.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Async("emailExecutor")
@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final JWTUtil jwtUtil;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, JWTUtil jwtUtil,
                        TemplateEngine templateEngine){
        this.javaMailSender = javaMailSender;
        this.jwtUtil = jwtUtil;
        this.templateEngine = templateEngine;
    }

    Logger logger = LoggerFactory.getLogger(EmailService.class);

    private void sendHTMLTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables){
        try{
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
        catch (MessagingException | MailException e){
            logger.error("Failed sending email to {} with subject: {}", to, subject);
            throw new EmailException("Az email elküldése sikertelen: " + e.getMessage());
        }
    }

    public String emailSentTimeStamp(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return LocalDateTime.now().format(formatter);
    }

    public void registerUser(String toEmail, String firstName, String lastName){
        Map<String, Object> variables = new HashMap<>();

        variables.put("firstName", firstName);
        variables.put("lastName", lastName);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Regisztráció", "emails/registration", variables);
    }

    public void loginUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Bejelentkezés", "emails/login", variables);
    }

    public void logoutUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Kijelentkezés", "emails/logout", variables);
    }

    public void banUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Bannolva lett a fiókod", "emails/banned", variables);
    }

    public void unbanUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "A fiókod újra engedélyezve lett", "emails/unbanned", variables);
    }

    public void updatePassword(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        String token = jwtUtil.generatePasswordUpdateToken(toEmail);

        String resetLink =
                "http://localhost:5173/resetPassword?token=" + token;
        variables.put("resetLink", resetLink);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail("metzroland1111@gmail.com", "Jelszó módosítás", "emails/passwordUpdate", variables);
    }

    public void updatePasswordSuccess(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Jelszó módosítás sikeres", "emails/passwordUpdateSuccess", variables);
    }

    public void updateUserDetailsSuccess(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Módosítás történ a felhasználói adatokban", "emails/userDetailsUpdated", variables);
    }

    public void updateUserEmailSuccess(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Email cím módosítva", "emails/userEmailUpdated", variables);
    }

    public void mfaEnabled(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "A kétfaktoros hitelesítés bekapcsolva", "emails/mfaEnabled", variables);
    }

    public void mfaDisabled(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "A kétfaktoros hitelesítés kikapcsolva", "emails/mfaDisabled", variables);
    }
}
