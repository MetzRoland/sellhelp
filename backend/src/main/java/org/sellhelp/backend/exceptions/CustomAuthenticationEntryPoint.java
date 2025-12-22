package org.sellhelp.backend.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sellhelp.backend.dtos.responses.GeneralErrorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Autowired
    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        GeneralErrorDTO errorDTO = new GeneralErrorDTO();
        errorDTO.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage("Csak bejelentkezett felhasználók részére!");

        response.getWriter().write(objectMapper.writeValueAsString(errorDTO));
    }
}

