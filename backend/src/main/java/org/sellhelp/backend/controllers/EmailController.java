package org.sellhelp.backend.controllers;

import org.sellhelp.backend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService){
        this.emailService = emailService;
    }

    @GetMapping("/send-email")
    public String sendEmail() {
        emailService.sendSimpleEmail(
                "metzroland1111@gmail.com",
                "Test Subject",
                "Hello!!! This is a plain text email from Spring Boot."
        );
        return "Email sent successfully!";
    }
}
