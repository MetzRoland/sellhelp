package org.sellhelp.backend.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sellhelp.backend.dtos.responses.GeneralErrorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Autowired
    public CustomAccessDeniedHandler(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        GeneralErrorDTO errorDTO = new GeneralErrorDTO();
        errorDTO.setStatus(HttpServletResponse.SC_FORBIDDEN);
        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage("Nincs jogosultságod a művelet végrehajtásához!");

        response.getWriter().write(objectMapper.writeValueAsString(errorDTO));
    }
}

