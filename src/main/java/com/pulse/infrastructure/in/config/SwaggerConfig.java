package com.pulse.infrastructure.in.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Pulse API - Sales Analysis")
                .description("API para análise de dados de vendas e identificação do melhor dia para promoções usando arquitetura hexagonal")
                .version("2.0.0")
                .contact(new Contact()
                    .name("Pulse Team")
                    .email("contato@pulse.com")
                    .url("https://pulse.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8081")
                    .description("Servidor de Desenvolvimento"),
                new Server()
                    .url("https://api.pulse.com")
                    .description("Servidor de Produção")
            ));
    }
} 