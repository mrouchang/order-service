package org.microservice.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Scopes scopes = new Scopes()
                .addString("openid", "OpenID Connect")
                .addString("profile", "User Profile")
                .addString("email", "User Email");

        OAuthFlow authorizationCodeFlow = new OAuthFlow()
                .authorizationUrl("http://localhost:8080/realms/minwei_keycloak/protocol/openid-connect/auth")
                .tokenUrl("http://localhost:8080/realms/minwei_keycloak/protocol/openid-connect/token")
                .refreshUrl("http://localhost:8080/realms/minwei_keycloak/protocol/openid-connect/token")
                .scopes(scopes);

        OAuthFlows oAuthFlows = new OAuthFlows()
                .authorizationCode(authorizationCodeFlow);

        SecurityScheme oauthSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("OAuth2 authentication via Keycloak")
                .flows(oAuthFlows);

        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0.0")
                        .description("Order Service API Documentation with OAuth2/Keycloak Security")
                        .contact(new Contact()
                                .name("API Support")
                                .url("https://yourcompany.com")
                                .email("support@yourcompany.com")))
                .addSecurityItem(new SecurityRequirement().addList("Keycloak OAuth2"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Keycloak OAuth2", oauthSecurityScheme));
    }
}
