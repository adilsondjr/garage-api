package br.com.teste.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Garage Management System API")
                .version("1.0.1")
                .description("Backend system for managing parking lots with dynamic pricing, real-time vehicle tracking, and revenue calculation")
                .contact(new Contact()
                    .name("Garage Management Team")
                    .url("https://github.com")
                    .email("support@garage.local"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
