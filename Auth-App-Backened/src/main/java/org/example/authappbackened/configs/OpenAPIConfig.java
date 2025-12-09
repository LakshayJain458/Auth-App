package org.example.authappbackened.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auth App Backend API",
                version = "1.0.0",
                description = """
                        Backend API for Authentication Application
                        
                        This API provides endpoints for user authentication, registration, and management.
                        It supports JWT-based authentication with both Bearer tokens and HTTP-only cookies.
                        
                        ## Authentication
                        - **Bearer JWT**: Include the JWT token in the Authorization header
                        - **Cookie JWT**: JWT refresh tokens are automatically sent via HTTP-only cookies
                        - **OAuth2**: Login via OAuth2 providers (Google, GitHub, etc.)
                        
                        ## Key Features
                        - User registration and login
                        - JWT access and refresh tokens
                        - Secure cookie-based authentication
                        - OAuth2 social login integration
                        - Role-based access control
                        """,
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        security = {
                @SecurityRequirement(name = "Bearer Authentication"),
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "Bearer Authentication",
                description = "JWT Bearer token authentication. Obtain the token from the login endpoint and include it in the Authorization header.",
                scheme = "bearer",
                type = SecuritySchemeType.HTTP,
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER
        )
})
public class OpenAPIConfig {
}
