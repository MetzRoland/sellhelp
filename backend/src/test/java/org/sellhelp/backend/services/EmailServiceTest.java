package org.sellhelp.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sellhelp.backend.exceptions.EmailException;
import org.sellhelp.backend.security.JWTUtil;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void init() {
        mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>dummy</html>");

        ReflectionTestUtils.setField(emailService, "fromEmail", "from@test.com");
    }

    @Test
    void registerUser_sendsEmail() {
        emailService.registerUser("test@test.com", "First", "Last");

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void loginUser_sendsEmail() {
        emailService.loginUser("test@test.com");
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void updatePassword_sendsEmailWithToken() {
        when(jwtUtil.generatePasswordUpdateToken("test@test.com")).thenReturn("token123");

        emailService.updatePassword("test@test.com");

        verify(javaMailSender).send(mimeMessage);
        verify(jwtUtil).generatePasswordUpdateToken("test@test.com");
    }

    @Test
    void emailExceptionThrown_whenMailFails() throws MessagingException {
        doThrow(new MailException("fail") {}).when(javaMailSender).send(any(MimeMessage.class));

        assertThrows(EmailException.class, () ->
                emailService.loginUser("test@test.com")
        );
    }
}

