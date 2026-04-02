package org.sellhelp.backend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@OpenAPIDefinition(
		info = @Info(
				title = "SellHelp API",
				version = "0.2",
				description = "API documentation for SellHelp backend"
		)
)
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Budapest"));

		SpringApplication.run(BackendApplication.class, args);
	}

}
