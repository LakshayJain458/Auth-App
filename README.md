<div align="center">

# 🛡️ AuthApp

### Secure Authentication, Made Simple

A **production-grade**, full-stack authentication system with JWT tokens, OAuth2 social login,
refresh-token rotation, and role-based access control — built with **Spring Boot 3** & **React 19**.

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Vite](https://img.shields.io/badge/Vite-7.3-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vite.dev)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

---

[Features](#-features) · [Architecture](#-architecture) · [Tech Stack](#-tech-stack) · [Getting Started](#-getting-started) · [API Reference](#-api-reference) · [Project Structure](#-project-structure) · [Screenshots](#-screenshots) · [Contributing](#-contributing)

</div>

---

## ✨ Features

| Category | Details |
|----------|---------|
| **JWT Authentication** | Stateless access tokens (1h) & refresh tokens (24h) signed with HMAC-SHA512. Auto-rotation on every refresh with revocation of stale tokens. |
| **OAuth2 Social Login** | One-click sign in with **Google** and **GitHub**. Automatic account linking with conflict-safe username generation. |
| **Refresh Token Rotation** | Every refresh issues a new token pair — old refresh tokens are revoked and chained via `replacedByToken` for audit trail. |
| **HTTP-Only Secure Cookies** | Refresh tokens delivered via `HttpOnly`, `SameSite=Lax`, `Secure`-ready cookies — immune to XSS. |
| **Role-Based Access Control** | `@ManyToMany` role system with dynamic authority mapping via Spring Security's `GrantedAuthority`. |
| **User Management CRUD** | Full lifecycle: create, read (by ID/email), update (selective fields), and delete with cascading token cleanup. |
| **Auto Token Refresh** | Axios interceptor queues 401'd requests, silently refreshes tokens, and retries — zero UX disruption. |
| **Glassmorphism UI** | Modern glass-card design with animated floating shapes, 3D tilt effects, theme toggle (light/dark), and responsive layout. |
| **Swagger / OpenAPI** | Interactive API docs at `/swagger-ui.html` with Bearer Auth scheme pre-configured. |
| **Multi-Profile Config** | Separate `application-{dev,qa,prod}.properties` with env-var overrides for every secret. |

---

## 🏛️ Architecture

```
┌────────────────────────┐         ┌──────────────────────────────────┐
│                        │  HTTPS  │                                  │
│   React 19 + Vite 7    │◄───────►│   Spring Boot 3.5.7 (REST API)  │
│                        │         │                                  │
│  • Axios + Interceptors│         │  • Spring Security 6             │
│  • React Router 7      │         │  • JWT (JJWT 0.13)              │
│  • Context API         │         │  • OAuth2 Client                │
│  • Lucide Icons        │         │  • Spring Data JPA              │
│  • Theme Toggle        │         │  • Bean Validation              │
│                        │         │  • Actuator + OpenAPI            │
└────────────────────────┘         └──────────┬───────────────────────┘
                                              │
                                              │ JDBC
                                              ▼
                                   ┌─────────────────────┐
                                   │   PostgreSQL 16      │
                                   │                     │
                                   │  • users            │
                                   │  • roles            │
                                   │  • user_roles       │
                                   │  • refresh_tokens   │
                                   └─────────────────────┘
```

### Authentication Flow

```
1. LOGIN
   Client ──POST /auth/login──► Server validates credentials
                                ├─► Generates access token (JWT, 1h)
                                ├─► Creates RefreshToken record in DB
                                ├─► Generates refresh token (JWT, 24h)
                                ├─► Sets refresh token as HttpOnly cookie
                                └─► Returns { accessToken, user } in body

2. AUTHENTICATED REQUEST
   Client ──Authorization: Bearer <access>──► JwtAuthenticationFilter
                                              ├─► Verify signature (HS512)
                                              ├─► Check token type = "access"
                                              ├─► Extract userId from subject
                                              ├─► Load User from DB
                                              └─► Set SecurityContext

3. TOKEN REFRESH (automatic via Axios interceptor on 401)
   Client ──POST /auth/refresh-token──► Server reads refresh token
           (cookie / body / header)     ├─► Validate: type, DB record, not revoked, not expired
                                        ├─► Revoke old token (replacedByToken = newJti)
                                        ├─► Issue new access + refresh pair
                                        └─► Return new tokens (cookie + body)

4. OAUTH2 (Google / GitHub)
   Client ──GET /oauth2/authorization/google──► Spring OAuth2 ──► Google
                                                                    │
   Client ◄──redirect /oauth/callback?token=<access>──◄────────────┘
              (refresh token in HttpOnly cookie)        OAuthSuccessHandler:
                                                        • Find or create user
                                                        • Generate JWT pair
                                                        • Redirect to frontend
```

---

## 🛠️ Tech Stack

### Backend

| Technology | Purpose |
|------------|---------|
| **Spring Boot 3.5.7** | Application framework |
| **Spring Security 6** | Authentication & authorization |
| **Spring Data JPA** | ORM & repository layer |
| **JJWT 0.13.0** | JWT creation, signing (HS512), parsing |
| **OAuth2 Client** | Google & GitHub social login |
| **PostgreSQL** | Primary database |
| **Lombok** | Boilerplate reduction |
| **ModelMapper 3.2.4** | Entity ↔ DTO mapping |
| **SpringDoc OpenAPI 2.8** | Swagger UI & API documentation |
| **Spring Actuator** | Health checks & monitoring |
| **Bean Validation** | Request payload validation |
| **BCrypt** | Password hashing |

### Frontend

| Technology | Purpose |
|------------|---------|
| **React 19.2** | UI library |
| **React Router 7** | Client-side routing |
| **Vite 7.3** | Build tool & dev server |
| **Axios 1.13** | HTTP client with interceptors |
| **Lucide React** | Icon library |
| **CSS Variables** | Design tokens & theming |
| **Context API** | Global auth state management |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| **Java** | 25+ |
| **Maven** | 3.9+ |
| **Node.js** | 20+ |
| **PostgreSQL** | 15+ |

### 1. Clone the repository

```bash
git clone https://github.com/your-username/Auth-App.git
cd Auth-App
```

### 2. Set up the database

```sql
CREATE DATABASE "auth-app-db";
```

### 3. Configure environment variables

Create a `.env` file or export the following:

```bash
# Database
DB_USER=your_postgres_user
DB_PASS=your_postgres_password

# JWT (optional — sensible defaults exist)
JWT_SECRET=YourSuperSecretKeyAtLeast64CharsLong...
JWT_ISSUER=auth-app
JWT_ACCESS_TTL_SECONDS=3600        # 1 hour
JWT_REFRESH_TTL_SECONDS=86400      # 24 hours

# OAuth2 (optional — only if you want social login)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# CORS / Frontend
CORS_ALLOWED_ORIGINS=http://localhost:5173
FRONTEND_URL=http://localhost:5173
```

### 4. Start the backend

```bash
cd Auth-App-Backened
./mvnw spring-boot:run
```

> Backend starts on **http://localhost:8083** (dev profile)
> Swagger UI available at **http://localhost:8083/swagger-ui.html**

### 5. Start the frontend

```bash
cd Auth-App-Frontend
npm install
npm run dev
```

> Frontend starts on **http://localhost:5173**

---

## 📡 API Reference

### Authentication — `/api/v1/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/register` | ✗ | Register a new user |
| `POST` | `/login` | ✗ | Authenticate & receive tokens |
| `POST` | `/logout` | ✗ | Revoke refresh token & clear cookie |
| `POST` | `/refresh-token` | ✗ | Rotate tokens (cookie/body/header) |

### Users — `/api/v1/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/add` | ✔ | Create a new user |
| `GET` | `/getAll` | ✔ | List all users |
| `GET` | `/getById/{id}` | ✔ | Get user by UUID |
| `GET` | `/getByEmail/{email}` | ✔ | Get user by email address |
| `PUT` | `/update/{id}` | ✔ | Update user fields |
| `DELETE` | `/delete/{id}` | ✔ | Delete user & revoke all tokens |

### OAuth2 — Handled by Spring Security

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/oauth2/authorization/google` | Initiate Google OAuth2 flow |
| `GET` | `/oauth2/authorization/github` | Initiate GitHub OAuth2 flow |

### Sample Request / Response

<details>
<summary><b>POST /api/v1/auth/login</b></summary>

**Request**
```json
{
  "email": "john@example.com",
  "password": "secret123"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "username": "john",
    "image": null,
    "enabled": true,
    "provider": "LOCAL",
    "roles": [{ "id": "...", "name": "ROLE_USER" }],
    "createdAt": "2026-02-11T10:00:00Z"
  }
}
```

**Headers**
```
Set-Cookie: refreshToken=eyJhb...; Path=/; HttpOnly; SameSite=Lax
```
</details>

<details>
<summary><b>POST /api/v1/auth/register</b></summary>

**Request**
```json
{
  "email": "jane@example.com",
  "username": "jane",
  "password": "secure456"
}
```

**Response** `201 Created`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "email": "jane@example.com",
  "username": "jane",
  "enabled": true,
  "provider": "LOCAL",
  "roles": [],
  "createdAt": "2026-02-11T10:05:00Z"
}
```
</details>

---

## 📂 Project Structure

```
Auth-App/
├── Auth-App-Backened/                   # Spring Boot backend
│   ├── pom.xml                          # Maven dependencies
│   └── src/main/java/org/example/authappbackened/
│       ├── AuthAppBackenedApplication.java
│       ├── configs/
│       │   ├── ApiConstant.java         # Public endpoint whitelist
│       │   ├── CorsConfig.java          # CORS configuration
│       │   ├── MapperConfig.java        # ModelMapper bean
│       │   ├── OAuthAuthenticationSuccessHandler.java
│       │   ├── OpenAPIConfig.java       # Swagger/OpenAPI setup
│       │   └── SecurityConfig.java      # Spring Security filter chain
│       ├── controllers/
│       │   ├── AuthController.java      # Login, register, logout, refresh
│       │   └── UserController.java      # User CRUD endpoints
│       ├── dtos/
│       │   ├── UserDto.java
│       │   └── RoleDto.java
│       ├── entities/
│       │   ├── User.java                # UserDetails implementation
│       │   ├── Role.java
│       │   ├── RefreshToken.java        # Token rotation tracking
│       │   └── enums/
│       │       ├── Provider.java        # LOCAL, GOOGLE, GITHUB
│       │       ├── LoginRequest.java
│       │       ├── TokenResponse.java
│       │       └── ErrorResponse.java
│       ├── exceptions/
│       │   ├── GlobalExceptionHandler.java
│       │   └── ResourceNotFoundException.java
│       ├── jwt/
│       │   ├── JwtService.java          # Token generation & validation
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── CookieService.java       # Refresh token cookie management
│       │   └── CustomUserDetailService.java
│       ├── repositories/
│       │   ├── UserRepo.java
│       │   └── RefreshTokenRepo.java
│       └── services/
│           ├── AuthService.java
│           ├── UserService.java
│           └── Impls/
│               ├── AuthServiceImpl.java
│               └── UserServiceImpl.java
│
├── Auth-App-Frontend/                   # React frontend
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx                     # App entry point
│       ├── App.jsx                      # Routes & providers
│       ├── index.css                    # Design tokens & theme
│       ├── context/
│       │   └── AuthContext.jsx          # Auth state management
│       ├── services/
│       │   └── api.js                   # Axios instance & interceptors
│       ├── pages/
│       │   ├── HomePage.jsx             # Landing page
│       │   ├── LoginPage.jsx            # Login form + OAuth buttons
│       │   ├── RegisterPage.jsx         # Registration form
│       │   ├── DashboardPage.jsx        # User profile dashboard
│       │   └── OAuthCallbackPage.jsx    # OAuth redirect handler
│       └── components/
│           ├── Navbar.jsx               # Navigation bar
│           ├── ProtectedRoute.jsx       # Auth guard
│           ├── Button.jsx               # Multi-variant button
│           ├── Input.jsx                # Floating-label input
│           ├── GlassCard.jsx            # Glassmorphism card + 3D tilt
│           ├── FloatingShapes.jsx       # Animated background
│           └── ThemeToggle.jsx          # Light/dark theme switch
│
└── README.md
```

---

## 🔒 Security Highlights

- **Password Hashing** — BCrypt with Spring Security's `BCryptPasswordEncoder`
- **Stateless Sessions** — No server-side session; all auth via JWT in `Authorization` header
- **Token Signing** — HMAC-SHA512 with ≥64-character secret key
- **HTTP-Only Cookies** — Refresh tokens stored in browser cookies inaccessible to JavaScript
- **CSRF Protection** — Stateless API with `SameSite=Lax` cookies (no CSRF tokens needed)
- **CORS Whitelisting** — Only configured origins allowed; credentials enabled
- **Token Rotation** — Each refresh invalidates the previous token; revocation chain tracked in DB
- **Input Validation** — `@Valid` + Bean Validation annotations on all DTOs
- **Error Sanitization** — `GlobalExceptionHandler` returns consistent JSON error responses; no stack traces leaked
- **OAuth2 Account Safety** — Username conflict resolution by appending provider suffix (e.g., `john_github`)

---

## 🧪 Running Tests

```bash
# Backend tests
cd Auth-App-Backened
./mvnw test

# Frontend lint
cd Auth-App-Frontend
npm run lint
```

---

## 📜 Environment Profiles

| Profile | Port | Use Case |
|---------|------|----------|
| `dev` | 8083 | Local development (default) |
| `qa` | — | QA / staging (configure as needed) |
| `prod` | — | Production deployment |

Switch profiles via:
```bash
spring.profiles.active=prod
# or
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feat/amazing-feature`
3. **Commit** your changes: `git commit -m 'feat: add amazing feature'`
4. **Push** to the branch: `git push origin feat/amazing-feature`
5. **Open** a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ by [Lakshay](https://github.com/your-username)**

If this project helped you, consider giving it a ⭐

</div>
