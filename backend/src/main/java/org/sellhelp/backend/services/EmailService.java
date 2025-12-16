package org.sellhelp.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final JWTUtil jwtUtil;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, JWTUtil jwtUtil){
        this.javaMailSender = javaMailSender;
        this.jwtUtil = jwtUtil;
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
    public void sendHTMLEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.setFrom(fromEmail);

        javaMailSender.send(message);
    }

    public void updatePassword(String email)
    {
        try {
            sendHTMLEmail(email, "Jelszó frissítése", tempHtmlTemplate(jwtUtil.generatePasswordUpdateToken(email)));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // temporary
    private String tempHtmlTemplate(String token)
    {
        return """
                <h1>Tisztelt Felhaszánló!</h1>
                <h2>Egy jelszófrissítést igényeltél. Az alábbi gombra kattitva továbbjutsz az űrlaphoz:</h2>
                <a href='sellhelp.org/user/update/password?q="""+token+"""
                ' style="padding:12px 20px; color:#0FFFF0; text-decoration:none; font-size:24px;">Űrlap a jelszó frissítéséhez</a>"""+
                "<br><pre>DEBUG TOKEN:"+token+"</pre>";
    }
}
