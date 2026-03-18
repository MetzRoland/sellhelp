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

        sendHTMLTemplateEmail(toEmail, "Regisztráció", "emails/usermanagement/registration", variables);
    }

    public void loginUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Bejelentkezés", "emails/usermanagement/login", variables);
    }

    public void logoutUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Kijelentkezés", "emails/usermanagement/logout", variables);
    }

    public void banUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Bannolva lett a fiókod", "emails/usermanagement/banned", variables);
    }

    public void unbanUser(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();

        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "A fiókod újra engedélyezve lett", "emails/usermanagement/unbanned", variables);
    }

    public void updatePassword(String toEmail, boolean forgotPassword)
    {
        Map<String, Object> variables = new HashMap<>();
        String token = jwtUtil.generatePasswordUpdateToken(toEmail);

        String resetLink =
                !forgotPassword ? "http://localhost:5173/resetPassword?token=" + token
                        : "http://localhost:5173/forgotPassword?token=" + token;
        variables.put("resetLink", resetLink);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail("metzroland1111@gmail.com", "Jelszó módosítás", "emails/usermanagement/passwordUpdate", variables);
    }

    public void updatePasswordSuccess(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Jelszó módosítás sikeres", "emails/usermanagement/passwordUpdateSuccess", variables);
    }

    public void updateUserDetailsSuccess(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Módosítás történ a felhasználói adatokban", "emails/usermanagement/userDetailsUpdated", variables);
    }

    public void updateUserEmailSuccess(String newEmail, String oldEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("newEmail", newEmail);
        variables.put("oldEmail", oldEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(newEmail, "Email cím módosítva", "emails/usermanagement/userEmailUpdated", variables);
    }

    public void mfaEnabled(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "A kétfaktoros hitelesítés bekapcsolva", "emails/usermanagement/mfaEnabled", variables);
    }

    public void mfaDisabled(String toEmail)
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "A kétfaktoros hitelesítés kikapcsolva", "emails/usermanagement/mfaDisabled", variables);
    }

    public void appliedToPost(String toEmail){
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Sikeres jelentkezés a posztra", "emails/postmanagement/appliedToPost", variables);
    }

    public void cancelAppliedToPost(String toEmail){
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Jelentkezés visszavonva a poszról", "emails/postmanagement/cancelAppliedToPost", variables);
    }

    public void gotSelectedToPost(String toEmail){
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Önt felvették a posztra", "emails/postmanagement/gotSelectedToPost", variables);
    }

    public void gotRejectedToPost(String toEmail){
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "Önt visszautasították utólag a posztról", "emails/postmanagement/gotRejectedToPost", variables);
    }

    public void cancelSelectedToPost(String toEmail){
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        sendHTMLTemplateEmail(toEmail, "A munkavállaló visszautasította a posztot", "emails/postmanagement/cancelSelectedToPost", variables);
    }

    public void changePostStatus(String toEmail, String postStatus){
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("timestamp", emailSentTimeStamp());

        switch (postStatus) {
            case "started" ->
                    sendHTMLTemplateEmail(toEmail, "A munkavállaló elkezdte a munkát", "emails/postmanagement/startedPost", variables);
            case "completed_by_employee" ->
                    sendHTMLTemplateEmail(toEmail, "A munkavállaló leadta a munkáját ellenőrzésre", "emails/postmanagement/sentWorkToCheck", variables);
            case "unsuccessful_result_closed" ->
                    sendHTMLTemplateEmail(toEmail, "A munkáltató lezárta a posztot sikertelen eredmény miatt", "emails/postmanagement/closedUnsuccessfully", variables);
            case "work_rejected" ->
                    sendHTMLTemplateEmail(toEmail, "A munkáltató visszadobta a munkát javításra", "emails/postmanagement/workRejected", variables);
            case "closed" ->
                    sendHTMLTemplateEmail(toEmail, "A munkáltató lezárta a posztot sikeres eredménnyel", "emails/postmanagement/closedSuccessfully", variables);
        }
    }
}
