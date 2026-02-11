# Auth-App Backend

A robust and secure Spring Boot authentication application with JWT-based authentication, OAuth2 integration, and comprehensive user management capabilities.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Security Implementation](#security-implementation)
- [Database Schema](#database-schema)
- [Authentication Flow](#authentication-flow)
- [Error Handling](#error-handling)

## 🔍 Overview

This is a production-ready Spring Boot backend application that provides secure authentication and user management services. The application implements JWT-based authentication with refresh token rotation, OAuth2 social login (Google, GitHub), and follows security best practices with stateless session management.

## ✨ Features

### Authentication & Security
- **JWT Authentication**: Stateless authentication using access and refresh tokens
- **Refresh Token Rotation**: Secure token refresh mechanism with automatic rotation and revocation
- **OAuth2 Integration**: Social login via Google and GitHub
- **Password Encryption**: BCrypt password hashing
- **HTTP-Only Cookies**: Secure refresh token storage
- **Token Revocation**: Manual logout with token invalidation
- **CORS Support**: Configurable cross-origin resource sharing

### User Management
- User registration with email validation
- User profile management (CRUD operations)
- Role-based access control (RBAC)
- Multi-provider support (Local, Google, GitHub)
- Account status management (enabled/disabled)

### Developer Features
- **Swagger/OpenAPI Documentation**: Interactive API documentation at `/swagger-ui.html`
- **Spring Boot Actuator**: Application monitoring and health checks
- **Comprehensive Logging**: SLF4J logging throughout the application
- **Profile-based Configuration**: Separate configurations for dev, QA, and prod environments
- **ModelMapper Integration**: Automatic DTO-Entity mapping

## 🛠 Technology Stack

### Core Framework
- **Spring Boot 3.5.7** - Main application framework
- **Java 25** - Programming language

### Security
- **Spring Security** - Authentication and authorization
- **Spring OAuth2 Client** - OAuth2 social login
- **JJWT 0.13.0** - JWT token generation and validation

### Data Layer
- **Spring Data JPA** - Data access abstraction
- **PostgreSQL** - Primary database
- **Hibernate** - ORM implementation
- **HikariCP** - Connection pooling

### Utilities
- **Lombok** - Boilerplate code reduction
- **ModelMapper 3.2.4** - Object mapping
- **Jakarta Validation** - Input validation

### Documentation & Monitoring
- **SpringDoc OpenAPI** - API documentation
- **Spring Boot Actuator** - Application monitoring

## 🏗 Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│         Controllers Layer           │
│  (AuthController, UserController)   │
└─────────────────────────────────────┘
               ↓
┌─────────────────────────────────────┐
│          Services Layer             │
│  (AuthService, UserService)         │
└─────────────────────────────────────┘
               ↓
┌─────────────────────────────────────┐
│        Repositories Layer           │
│  (UserRepo, RefreshTokenRepo)       │
└─────────────────────────────────────┘
               ↓
┌─────────────────────────────────────┐
│          Database Layer             │
│         (PostgreSQL)                │
└─────────────────────────────────────┘
```

### Security Filter Chain

```
HTTP Request
    ↓
JwtAuthenticationFilter (Bearer token validation)
    ↓
SecurityFilterChain (Authorization rules)
    ↓
Controller (Protected endpoints)
    ↓
HTTP Response
```

## 📁 Project Structure

```
src/main/java/org/example/authappbackened/
├── configs/
│   ├── ApiConstant.java                      # API endpoint constants
│   ├── MapperConfig.java                     # ModelMapper configuration
│   ├── OAuthAuthenticationSuccessHandler.java # OAuth2 success handler
│   ├── OpenAPIConfig.java                    # Swagger/OpenAPI configuration
│   └── SecurityConfig.java                   # Spring Security configuration
├── controllers/
│   ├── AuthController.java                   # Authentication endpoints
│   └── UserController.java                   # User management endpoints
├── dtos/
│   ├── RoleDto.java                          # Role data transfer object
│   └── UserDto.java                          # User data transfer object
├── entities/
│   ├── RefreshToken.java                     # Refresh token entity
│   ├── Role.java                             # Role entity
│   ├── User.java                             # User entity (implements UserDetails)
│   └── enums/
│       ├── ErrorResponse.java                # Error response structure
│       ├── LoginRequest.java                 # Login request record
│       ├── Provider.java                     # Authentication provider enum
│       └── TokenResponse.java                # Token response structure
├── exceptions/
│   ├── GlobalExceptionHandler.java           # Centralized exception handling
│   └── ResourceNotFoundException.java        # Custom exception
├── jwt/
│   ├── CookieService.java                    # Cookie management for refresh tokens
│   ├── CustomUserDetailService.java          # UserDetailsService implementation
│   ├── JwtAuthenticationFilter.java          # JWT validation filter
│   └── JwtService.java                       # JWT generation and parsing
├── repositories/
│   ├── RefreshTokenRepo.java                 # Refresh token repository
│   └── UserRepo.java                         # User repository
├── services/
│   ├── AuthService.java                      # Authentication service interface
│   ├── UserService.java                      # User service interface
│   └── Impls/
│       ├── AuthServiceImpl.java              # Authentication service implementation
│       └── UserServiceImpl.java              # User service implementation
└── AuthAppBackenedApplication.java           # Application entry point

src/main/resources/
├── application.properties                     # Main configuration
├── application-dev.properties                 # Development environment config
├── application-qa.properties                  # QA environment config
└── application-prod.properties                # Production environment config
```

## 🚀 Getting Started

### Prerequisites
- Java 25 or higher
- Maven 3.6+
- PostgreSQL 12+
- (Optional) Google OAuth2 credentials
- (Optional) GitHub OAuth2 credentials

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Auth-App-Backened
   ```

2. **Configure the database**
   ```sql
   CREATE DATABASE auth_app_db;
   ```

3. **Set environment variables**
   ```bash
   # Database credentials
   export DB_USER=your_db_username
   export DB_PASS=your_db_password
   
   # JWT configuration
   export JWT_SECRET=YourSecretKeyMustBeAtLeast64CharactersLongForHS512Algorithm12345
   export JWT_ISSUER=auth-app
   export JWT_ACCESS_TTL_SECONDS=3600
   export JWT_REFRESH_TTL_SECONDS=86400
   
   # OAuth2 credentials (optional)
   export GOOGLE_CLIENT_ID=your_google_client_id
   export GOOGLE_CLIENT_SECRET=your_google_client_secret
   export GITHUB_CLIENT_ID=your_github_client_id
   export GITHUB_CLIENT_SECRET=your_github_client_secret
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or specify a profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

6. **Access the application**
   - API Base URL: `http://localhost:8083`
   - Swagger UI: `http://localhost:8083/swagger-ui.html`
   - API Docs: `http://localhost:8083/v3/api-docs`

## ⚙️ Configuration

### Application Profiles

The application supports three environments:
- **dev** - Development environment (port 8083)
- **qa** - Quality assurance environment
- **prod** - Production environment

Set the active profile in `application.properties`:
```properties
spring.profiles.active=dev
```

### Key Configuration Properties

#### Database Configuration (application-dev.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth-app-db
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.jpa.hibernate.ddl-auto=update
```

#### JWT Configuration
```properties
security.jwt.secret=${JWT_SECRET}
security.jwt.issuer=${JWT_ISSUER:auth-app}
security.jwt.access-ttl-seconds=${JWT_ACCESS_TTL_SECONDS:3600}
security.jwt.refresh-ttl-seconds=${JWT_REFRESH_TTL_SECONDS:86400}
security.jwt.refresh-token-cookie-name=${JWT_REFRESH_COOKIE_NAME:refreshToken}
security.jwt.cookie-secure=${JWT_COOKIE_SECURE:true}
security.jwt.cookie-http-only=${JWT_COOKIE_HTTP_ONLY:true}
security.jwt.cookie-same-site=${JWT_COOKIE_SAME_SITE:lax}
```

#### OAuth2 Configuration
```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=email,profile,openid

# GitHub OAuth2
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.scope=user,email
```

## 📡 API Endpoints

### Authentication Endpoints (`/api/v1/auth`)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/register` | Register a new user | Public |
| POST | `/login` | Login with email/password | Public |
| POST | `/logout` | Logout and revoke refresh token | Public |
| POST | `/refresh-token` | Refresh access token | Public |

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "username",
  "password": "securePassword123"
}
```

**Response (201 Created):**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "username": "username",
  "enabled": true,
  "provider": "LOCAL",
  "createdAt": "2025-12-09T10:00:00Z",
  "updatedAt": "2025-12-09T10:00:00Z",
  "roles": []
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "username": "username",
    "enabled": true
  }
}
```
*Note: Refresh token is also set as HTTP-only cookie*

#### Logout
```http
POST /api/v1/auth/logout
Cookie: refreshToken=eyJhbGciOiJIUzUxMiJ9...
```

**Response (204 No Content)**

#### Refresh Token
```http
POST /api/v1/auth/refresh-token
Cookie: refreshToken=eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 3600,
  "user": { ... }
}
```

### User Management Endpoints (`/api/v1/users`)

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/add` | Create a new user | Required |
| GET | `/getAll` | Get all users | Required |
| GET | `/getById/{id}` | Get user by ID | Required |
| GET | `/getByEmail/{email}` | Get user by email | Required |
| PUT | `/update/{id}` | Update user | Required |
| DELETE | `/delete/{id}` | Delete user | Required |

*Note: All user management endpoints require JWT authentication via Bearer token in Authorization header*

#### Get All Users
```http
GET /api/v1/users/getAll
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### Get User by ID
```http
GET /api/v1/users/getById/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### Update User
```http
PUT /api/v1/users/update/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "username": "newUsername",
  "email": "newemail@example.com"
}
```

#### Delete User
```http
DELETE /api/v1/users/delete/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### OAuth2 Login

**Google Login:**
```
GET /oauth2/authorization/google
```

**GitHub Login:**
```
GET /oauth2/authorization/github
```

## 🔒 Security Implementation

### JWT Token System

#### Access Token
- **Type**: Bearer token
- **Lifetime**: 1 hour (configurable)
- **Usage**: Sent in Authorization header for API requests
- **Claims**: 
  - `sub`: User ID
  - `email`: User email
  - `roles`: User roles
  - `type`: "access"
  - `iss`: Issuer
  - `iat`: Issued at
  - `exp`: Expiration time
  - `jti`: JWT ID

#### Refresh Token
- **Type**: Opaque token stored in HTTP-only cookie
- **Lifetime**: 24 hours (configurable)
- **Usage**: Automatically sent via cookie to refresh endpoint
- **Storage**: Database-backed with revocation support
- **Claims**:
  - `sub`: User ID
  - `type`: "refresh"
  - `jti`: JWT ID (links to database record)

### Security Features

1. **Stateless Authentication**: No server-side session storage
2. **Token Rotation**: New refresh token issued on every refresh
3. **Token Revocation**: Refresh tokens can be revoked on logout
4. **Password Encryption**: BCrypt with 10 rounds
5. **HTTP-Only Cookies**: Prevents XSS attacks on refresh tokens
6. **CORS Protection**: Configurable CORS policies
7. **Input Validation**: Jakarta validation annotations
8. **Exception Handling**: Centralized error handling

### Security Filter Chain

```java
SecurityFilterChain:
1. CSRF disabled (stateless API)
2. CORS enabled with defaults
3. Session management: STATELESS
4. Public endpoints: /api/v1/auth/-, /swagger-ui/-, /v3/api-docs/-
5. All other endpoints: AUTHENTICATED
6. OAuth2 login configured with success handler
7. JWT filter before UsernamePasswordAuthenticationFilter
8. Custom authentication entry point for 401 errors
```

## 🗄 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) UNIQUE NOT NULL,
    username VARCHAR(64) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    image VARCHAR(1024),
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255)
);
```

### Roles Table
```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);
```

### User_Roles Join Table
```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Refresh_Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    jwt_id VARCHAR(255) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    replaced_by_token VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_token_jwt_id ON refresh_tokens(jwt_id);
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);
```

## 🔄 Authentication Flow

### Registration Flow
```
1. User submits registration form (email, username, password)
2. AuthController receives request
3. AuthService validates input
4. Password is hashed using BCrypt
5. UserService creates user with LOCAL provider
6. User entity saved to database
7. UserDto returned (without password)
```

### Login Flow (Local)
```
1. User submits login credentials (email, password)
2. AuthController validates input
3. AuthenticationManager authenticates user
4. On success, retrieve User from database
5. Generate JWT ID and create RefreshToken entity
6. Save RefreshToken to database
7. Generate access token (short-lived)
8. Generate refresh token (long-lived, contains JWT ID)
9. Attach refresh token as HTTP-only cookie
10. Return access token and user info in response body
```

### OAuth2 Login Flow
```
1. User clicks "Login with Google/GitHub"
2. Redirect to OAuth provider
3. User authorizes application
4. Provider redirects back with authorization code
5. OAuthAuthenticationSuccessHandler invoked
6. Extract user info from OAuth provider
7. Check if user exists in database
8. If new user, create account with provider details
9. Generate JWT tokens
10. Save refresh token to database
11. Return access token in response
```

### Token Refresh Flow
```
1. Client detects access token expiration
2. Client calls /refresh-token with refresh token (cookie or body)
3. AuthController extracts refresh token
4. Validate token is of type "refresh"
5. Extract JWT ID and User ID from token
6. Query database for RefreshToken by JWT ID
7. Validate:
   - Token exists in database
   - User ID matches
   - Token not revoked
   - Token not expired
8. Revoke old refresh token (set revoked=true)
9. Generate new JWT ID
10. Create new RefreshToken entity with new JWT ID
11. Save new refresh token to database
12. Generate new access token
13. Generate new refresh token
14. Return new tokens to client
```

### Logout Flow
```
1. Client calls /logout endpoint
2. Extract refresh token from cookie or request
3. If refresh token exists:
   - Parse token to get JWT ID
   - Find RefreshToken in database by JWT ID
   - Mark token as revoked
   - Save to database
4. Clear refresh token cookie
5. Clear SecurityContext
6. Return 204 No Content
```

### Request Authentication Flow
```
1. Client sends request with Authorization header
2. JwtAuthenticationFilter intercepts request
3. Extract Bearer token from header
4. Validate token is of type "access"
5. Parse token and extract User ID
6. Query database for User by ID
7. Validate user exists and is enabled
8. Create Authentication object with user details
9. Set authentication in SecurityContext
10. Continue with request processing
```

## 🚨 Error Handling

### Global Exception Handler

The application uses `@RestControllerAdvice` for centralized exception handling:

#### Exception Types

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `ResourceNotFoundException` | 404 | Requested resource not found |
| `IllegalArgumentException` | 400 | Invalid input parameters |
| `BadCredentialsException` | 401 | Invalid credentials |
| `DisabledException` | 401 | User account disabled |
| `AuthenticationException` | 401 | General authentication error |
| `Exception` | 500 | Unexpected server error |

#### Error Response Format
```json
{
  "message": "Error description",
  "status": "HTTP_STATUS_NAME",
  "statusCode": 400
}
```

### Common Error Scenarios

#### Invalid Credentials
```json
{
  "message": "Invalid credentials",
  "status": "UNAUTHORIZED",
  "statusCode": 401
}
```

#### Token Expired
```json
{
  "message": "JWT token has expired",
  "status": "UNAUTHORIZED",
  "statusCode": 401
}
```

#### User Not Found
```json
{
  "message": "User not found with id: ...",
  "status": "NOT_FOUND",
  "statusCode": 404
}
```

#### Validation Error
```json
{
  "message": "Password cannot be null or empty",
  "status": "BAD_REQUEST",
  "statusCode": 400
}
```

## 📝 Code Quality Features

### Logging Strategy
- **SLF4J with Lombok**: `@Slf4j` annotation on all classes
- **Log Levels**:
  - `INFO`: Successful operations (login, registration, CRUD)
  - `DEBUG`: Detailed flow information
  - `WARN`: Potential issues (expired tokens, disabled accounts)
  - `ERROR`: Exception scenarios with stack traces
- **Structured Logging**: Consistent format with contextual information

### Best Practices Implemented
- **DTO Pattern**: Separation of API models from domain entities
- **Service Layer**: Business logic separated from controllers
- **Repository Pattern**: Data access abstraction
- **Builder Pattern**: Lombok builders for immutable object creation
- **Constructor Injection**: Preferred over field injection
- **Interface Segregation**: Separate interfaces for services
- **Single Responsibility**: Each class has one clear purpose

## 🔧 Development Tips

### Adding a New Role
```java
// In database or via migration
Role adminRole = Role.builder()
    .name("ROLE_ADMIN")
    .build();
roleRepository.save(adminRole);
```

### Customizing Token Lifetime
Modify in `application-dev.properties`:
```properties
security.jwt.access-ttl-seconds=7200  # 2 hours
security.jwt.refresh-ttl-seconds=604800  # 7 days
```

### Adding Custom Claims to JWT
Modify `JwtService.generateAccessToken()`:
```java
.claims(Map.of(
    CLAIM_EMAIL, user.getEmail(),
    CLAIM_ROLES, roles,
    CLAIM_TYPE, TOKEN_TYPE_ACCESS,
    "customClaim", customValue  // Add here
))
```

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Introduction](https://jwt.io/introduction)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [OAuth 2.0 Overview](https://oauth.net/2/)

## 🤝 Contributing

1. Follow the existing code structure
2. Maintain logging standards
3. Write meaningful commit messages
4. Ensure all tests pass before committing
5. Update documentation for new features

## 📄 License

This project is part of a learning/development portfolio.

## 👤 Author

Lakshay

---

**Note**: This is a backend-only application. A separate frontend application is required to consume these APIs.

