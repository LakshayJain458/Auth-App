# 📖 AuthApp — Codebase Deep Dive

> A complete technical walkthrough of the entire AuthApp project — how every file works, how they
> connect, and the reasoning behind each architectural decision. Read this to understand the
> codebase as if you wrote it yourself.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Backend Architecture](#2-backend-architecture)
   - [Application Bootstrap](#21-application-bootstrap)
   - [Configuration Layer](#22-configuration-layer)
   - [Entity Layer (Database Models)](#23-entity-layer)
   - [Repository Layer (Data Access)](#24-repository-layer)
   - [Service Layer (Business Logic)](#25-service-layer)
   - [Controller Layer (API Endpoints)](#26-controller-layer)
   - [JWT Module (Token Engine)](#27-jwt-module)
   - [Exception Handling](#28-exception-handling)
   - [DTOs (Data Transfer Objects)](#29-dtos)
3. [Frontend Architecture](#3-frontend-architecture)
   - [Entry Point & Routing](#31-entry-point--routing)
   - [Auth State Management (Context)](#32-auth-state-management)
   - [API Layer (Axios)](#33-api-layer)
   - [Pages](#34-pages)
   - [Reusable Components](#35-reusable-components)
   - [Styling & Theme System](#36-styling--theme-system)
4. [Authentication Flows](#4-authentication-flows)
   - [Registration Flow](#41-registration-flow)
   - [Login Flow](#42-login-flow)
   - [Token Refresh Flow](#43-token-refresh-flow)
   - [OAuth2 Flow](#44-oauth2-flow)
   - [Logout Flow](#45-logout-flow)
5. [Data Model (Database Schema)](#5-data-model)
6. [Security Architecture](#6-security-architecture)
7. [Environment & Configuration](#7-environment--configuration)
8. [File-by-File Reference](#8-file-by-file-reference)

---

## 1. Project Overview

AuthApp is a **full-stack authentication system** with two independently runnable modules:

```
Auth-App/
├── Auth-App-Backened/    → Spring Boot 3.5.7 REST API (Java 25)
└── Auth-App-Frontend/    → React 19 SPA (Vite 7)
```

**What it does:**
- User registration with email/password (BCrypt hashed)
- Login that returns JWT access token (1h) + refresh token (24h, HttpOnly cookie)
- OAuth2 social login via Google and GitHub
- Automatic token refresh with rotation (old tokens revoked)
- User profile management (CRUD)
- Role-based access control
- Light/dark theme with glassmorphism UI

**How they communicate:**
- Frontend (`:5173`) ↔ Backend (`:8083`) via REST JSON APIs
- CORS configured to allow the frontend origin
- Access token sent via `Authorization: Bearer` header
- Refresh token sent via HttpOnly cookie (automatic on every request due to `withCredentials: true`)

---

## 2. Backend Architecture

### 2.1 Application Bootstrap

**File:** `AuthAppBackenedApplication.java`

Standard Spring Boot entry point. The `@SpringBootApplication` annotation triggers:
- Component scanning (finds all `@Service`, `@Controller`, `@Configuration`, `@Component`, `@Repository`)
- Auto-configuration (sets up JPA, Security, Web, OAuth2, Actuator based on classpath)
- Property loading (`application.properties` → `application-dev.properties` based on active profile)

**Application Properties:**
- `application.properties` — Sets app name, base port (`8082`), and active profile (`dev`)
- `application-dev.properties` — Overrides port to `8083`, configures PostgreSQL, JWT secrets, OAuth2 clients, CORS origins
- `application-qa.properties` / `application-prod.properties` — Empty placeholders for future environments

---

### 2.2 Configuration Layer

All in `configs/` package. These are `@Configuration` or `@Component` classes that Spring loads at startup.

#### `SecurityConfig.java` — The Security Brain

This is the **most critical config file**. It defines the entire security filter chain:

```
HTTP Request
  │
  ├─ CORS filter (from CorsConfig)
  ├─ CSRF disabled (stateless API with cookies — SameSite protects us)
  ├─ Session: STATELESS (no server-side sessions, all auth via JWT)
  │
  ├─ Public endpoints (no auth required):
  │   /api/v1/auth/**     ← login, register, logout, refresh
  │   /swagger-ui/**      ← API docs
  │   /v3/api-docs/**     ← OpenAPI spec
  │   /swagger-ui.html    ← Swagger entry
  │
  ├─ Everything else → requires authentication
  │
  ├─ OAuth2 Login:
  │   Success → OAuthAuthenticationSuccessHandler
  │   Failure → redirect to frontend /login?error=oauth_failed
  │
  ├─ Exception handling:
  │   Unauthenticated requests → JSON { message, status, code } (not HTML)
  │
  └─ JwtAuthenticationFilter (inserted BEFORE UsernamePasswordAuthenticationFilter)
```

**Key beans it provides:**
- `BCryptPasswordEncoder` — for hashing passwords
- `AuthenticationManager` — for programmatic login (used in `AuthController.login()`)

#### `CorsConfig.java` — Cross-Origin Rules

Creates a `CorsConfigurationSource` bean that:
- Reads allowed origins from `app.cors.allowed-origins` property (comma-separated)
- Allows methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
- Allows headers: Authorization, Content-Type, X-Refresh-Token, etc.
- `allowCredentials(true)` — required for cookies to be sent cross-origin
- Max preflight cache: 3600 seconds

**Why this matters:** The frontend on `:5173` calls the backend on `:8083`. Without CORS config, browsers block these requests.

#### `OAuthAuthenticationSuccessHandler.java` — OAuth2 Completion

When Google/GitHub OAuth2 finishes successfully, Spring Security calls this handler. Here's what it does:

1. **Extract user info from the OAuth2 principal:**
   - Google: `email`, `name`, `sub` (unique Google ID), `picture`
   - GitHub: `email` (can be null), `login` (username), `id`, `avatar_url`
   
2. **Find or create the user in the database:**
   - Searches by email first
   - If not found, creates a new user with the provider's info
   - If username conflicts, appends `_google` or `_github` suffix
   
3. **Generate tokens:**
   - Creates `RefreshToken` DB record
   - Generates JWT access + refresh tokens
   
4. **Set refresh token as HttpOnly cookie**

5. **Redirect to frontend:** `http://localhost:5173/oauth/callback?token=<accessToken>`
   - The access token goes in the URL (short-lived, 1h)
   - The refresh token goes in the cookie (long-lived, 24h, HttpOnly)

#### `MapperConfig.java` — ModelMapper Setup

Provides a `ModelMapper` bean with explicit field mappings:
- `User.name` → `UserDto.username`
- `User.email` → `UserDto.email`

ModelMapper is used throughout the service layer to convert between entities and DTOs.

#### `OpenAPIConfig.java` — Swagger Setup

Configures the Swagger UI at `/swagger-ui.html`:
- API title, version, description
- Bearer JWT authentication scheme
- Contact info and license

#### `ApiConstant.java` — Endpoint Whitelist

Defines which URL patterns skip authentication:
```java
String[] Auth_Public_Endpoints = {
    "/api/v1/auth/**",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/swagger-ui.html"
};
```

---

### 2.3 Entity Layer

All in `entities/` package. These are JPA `@Entity` classes that map directly to PostgreSQL tables.

#### `User.java` — The Core Entity

Maps to the `users` table. Implements Spring Security's `UserDetails` interface.

**Key fields:**
| Field | Type | Notes |
|-------|------|-------|
| `id` | `UUID` | Auto-generated on persist |
| `email` | `String` | Unique, validated (`@Email @NotBlank`), max 320 |
| `username` | `String` | Unique, max 64 |
| `password` | `String` | BCrypt hash, max 255 |
| `image` | `String` | Avatar URL (from OAuth providers), max 1024 |
| `enabled` | `boolean` | Default `true` |
| `createdAt` | `Instant` | Set via `@PrePersist` |
| `updatedAt` | `Instant` | Set via `@PreUpdate` |
| `provider` | `Provider` | Enum: `LOCAL`, `GOOGLE`, `GITHUB` |
| `providerId` | `String` | External provider's unique user ID |
| `roles` | `Set<Role>` | `@ManyToMany` with `EAGER` fetch, join table `user_roles` |

**Important `UserDetails` methods:**
- `getUsername()` returns the **email** (Spring Security uses this for authentication)
- `getName()` returns the actual username (custom method)
- `getAuthorities()` maps roles to `SimpleGrantedAuthority`

#### `Role.java`

Maps to the `roles` table. Simple: `UUID id` + `String name` (e.g., `ROLE_USER`, `ROLE_ADMIN`).

#### `RefreshToken.java`

Maps to `refresh_tokens` table. Tracks every issued refresh token.

**Key fields:**
| Field | Type | Notes |
|-------|------|-------|
| `id` | `UUID` | Primary key |
| `jwtId` | `String` | The JTI claim in the JWT — unique index |
| `user` | `User` | `@ManyToOne LAZY` |
| `createdAt` | `Instant` | When issued |
| `expiresAt` | `Instant` | When it expires |
| `revoked` | `boolean` | `true` after rotation or logout |
| `replacedByToken` | `String` | Points to the new JTI that replaced this one |

**Why store refresh tokens in DB?** To enable:
- Token revocation (logout invalidates the token)
- Token rotation (each refresh creates a new token and revokes the old one)
- Audit trail (chain of `replacedByToken` references)

#### `enums/` — Request & Response Records

| File | Purpose |
|------|---------|
| `Provider.java` | Enum: `LOCAL`, `GOOGLE`, `GITHUB` |
| `LoginRequest.java` | Java record: `{ email, password }` |
| `TokenResponse.java` | Java record: `{ accessToken, refreshToken, tokenType, expiresIn, user }` with factory method |
| `ErrorResponse.java` | Java record: `{ message, status, code }` — standardized error format |

---

### 2.4 Repository Layer

Spring Data JPA repositories — interfaces that Spring auto-implements at runtime.

#### `UserRepo.java`
```java
JpaRepository<User, UUID>
├── findByEmail(String email)           → Optional<User>
├── existsByEmail(String email)         → boolean
├── findUserByEmail(String email)       → Optional<User>
└── findByUsername(String username)      → Optional<User>
```

#### `RefreshTokenRepo.java`
```java
JpaRepository<RefreshToken, UUID>
├── findByJwtId(String jwtId)           → Optional<RefreshToken>
└── deleteByUserId(UUID userId)         → void (bulk delete on user removal)
```

---

### 2.5 Service Layer

Follows the **interface + implementation** pattern for clean dependency injection and testability.

#### `AuthService` → `AuthServiceImpl`

Single method: `registerUser(UserDto)`:
1. Validates the DTO and password are not null
2. Hashes the password with `BCryptPasswordEncoder`
3. Delegates to `UserService.createUser()` for persistence
4. Returns the created `UserDto`

#### `UserService` → `UserServiceImpl`

Full CRUD:

| Method | What It Does |
|--------|-------------|
| `createUser(UserDto)` | Validates DTO, checks email uniqueness, maps to entity, sets `Provider.LOCAL` if null, saves |
| `getAllUsers()` | Fetches all, maps to DTOs |
| `getUserById(String)` | Parses UUID string, finds or throws `ResourceNotFoundException` |
| `getUserByEmail(String)` | Finds by email or throws |
| `updateUser(String, UserDto)` | **Selective updates** — only overwrites fields that are non-null in the DTO (username, email, password, image, provider) |
| `deleteUser(String)` | First deletes all refresh tokens for the user (`@Transactional`), then deletes the user |

**Why selective updates?** The frontend might only send `{ username: "new" }` without including email. We don't want to null out unchanged fields.

---

### 2.6 Controller Layer

RESTful controllers that map HTTP endpoints to service methods.

#### `AuthController` — `/api/v1/auth`

| Endpoint | Method | Logic |
|----------|--------|-------|
| `/register` | POST | Delegates to `AuthService.registerUser()` → returns `201` |
| `/login` | POST | Authenticates via `AuthenticationManager`, creates refresh token in DB, generates JWT pair, sets cookie → returns `TokenResponse` |
| `/logout` | POST | Reads refresh token from request (multi-source), revokes it in DB, clears cookie, clears SecurityContext → returns `204` |
| `/refresh-token` | POST | Reads refresh token (multi-source), validates against DB (not revoked, not expired, user matches), revokes old + creates new → returns new `TokenResponse` |

**Multi-source refresh token reading** (`readRefreshTokenFromRequest`):
The method tries to find the refresh token in this priority order:
1. **HTTP cookie** — primary source (set by the server itself)
2. **Request body** — fallback for clients that can't send cookies
3. **`X-Refresh-Token` header** — custom header option
4. **`Authorization: Bearer` header** — last resort, only if the token is a refresh type

This flexibility ensures the refresh works across different client types (browser, mobile, Postman).

#### `UserController` — `/api/v1/users`

Standard CRUD: `POST /add`, `GET /getAll`, `GET /getById/{id}`, `GET /getByEmail/{email}`, `PUT /update/{id}`, `DELETE /delete/{id}`.

All endpoints require authentication (not in the public endpoint whitelist).

---

### 2.7 JWT Module

The `jwt/` package handles everything related to JSON Web Tokens.

#### `JwtService.java` — Token Generation & Parsing

**Constructor setup:**
- Reads `security.jwt.secret` (must be ≥64 chars)
- Converts to HMAC-SHA512 signing key
- Reads TTL values for access (3600s) and refresh (86400s) tokens
- Reads issuer name (e.g., `auth-app`)

**Token generation:**

Both token types share: `issuer`, `issuedAt`, `expiration`, `subject = userId`, signed with HS512.

| Token Type | Unique Claims |
|------------|---------------|
| **Access** | `email`, `roles` (list), `type: "access"` |
| **Refresh** | `jti: <jwtId>` (links to DB), `type: "refresh"` |

**Key methods:**
- `generateAccessToken(User)` → JWT string
- `generateRefreshToken(User, jwtId)` → JWT string
- `parse(token)` → `Claims` (throws if expired/invalid)
- `isAccessToken(token)` / `isRefreshToken(token)` → checks `type` claim
- `getUserId(token)` → UUID from subject
- `getJwtId(token)` → JTI claim

#### `JwtAuthenticationFilter.java` — Request Authentication

Extends `OncePerRequestFilter` — runs on every HTTP request.

**Logic:**
```
1. Extract "Authorization: Bearer <token>" header
2. If no header → skip (request continues as anonymous)
3. Verify token is an ACCESS token (not refresh)
4. Parse userId from subject claim
5. Load User from database
6. Check user.isEnabled()
7. Create UsernamePasswordAuthenticationToken with user + authorities
8. Set it in SecurityContextHolder
9. Continue filter chain
```

**Error handling:** Catches `ExpiredJwtException` and `JwtException` — logs warning and continues (request proceeds as unauthenticated, which Spring Security then blocks if the endpoint requires auth).

#### `CookieService.java` — Cookie Management

Handles the refresh token cookie lifecycle:
- `attachRefreshTokenCookie(response, token, maxAge)` — creates a `ResponseCookie` with all security attributes (HttpOnly, SameSite, Secure, domain, path) and adds it to the response
- `clearRefreshTokenCookie(response)` — same cookie with `maxAge=0` (browser deletes it)
- `addNoCacheHeaders(response)` — adds `Cache-Control: no-cache` and `Pragma: no-cache` to prevent caching of auth responses

All cookie properties are configurable via application properties.

#### `CustomUserDetailService.java`

Implements Spring Security's `UserDetailsService`. The single method `loadUserByUsername(email)` finds a user by email. This is used internally by Spring's `AuthenticationManager` during login.

---

### 2.8 Exception Handling

#### `GlobalExceptionHandler.java` — `@RestControllerAdvice`

Catches all exceptions and returns consistent JSON:

```json
{ "message": "...", "status": "BAD_REQUEST", "code": 400 }
```

| Exception | HTTP Status | When |
|-----------|------------|------|
| `ResourceNotFoundException` | 404 | User/entity not found |
| `IllegalArgumentException` | 400 | Invalid input |
| `BadCredentialsException` | 401 | Wrong password, invalid token |
| `DisabledException` | 401 | Account disabled |
| `AuthenticationException` | 401 | General auth failure |
| `JwtException` | 401 | Expired/tampered token |
| `Exception` (catch-all) | 500 | Unexpected errors |

**Why this matters:** Without this, Spring would return HTML error pages or expose stack traces. This ensures every error is machine-readable JSON.

---

### 2.9 DTOs

#### `UserDto.java`

Data Transfer Object for User. Mirrors the entity but:
- `password` has `@JsonProperty(access = WRITE_ONLY)` — **never exposed in API responses**
- Includes `Set<RoleDto> roles`
- Built with `@Data @Builder` (Lombok generates getters, setters, builder)

#### `RoleDto.java`

Simple: `UUID id` + `String name`.

---

## 3. Frontend Architecture

### 3.1 Entry Point & Routing

#### `main.jsx`

Minimal entry: renders `<App />` inside `<StrictMode>` into `<div id="root">`.

#### `App.jsx`

Sets up the app structure:

```
<BrowserRouter>
  <AuthProvider>         ← makes auth state available everywhere
    <Navbar />           ← always rendered (hides itself on auth pages)
    <Routes>
      /                  → <HomePage />
      /login             → <LoginPage />      (redirects to /dashboard if logged in)
      /register          → <RegisterPage />   (redirects to /dashboard if logged in)
      /oauth/callback    → <OAuthCallbackPage />
      /dashboard         → <ProtectedRoute><DashboardPage /></ProtectedRoute>
      *                  → <Navigate to="/" />
    </Routes>
  </AuthProvider>
</BrowserRouter>
```

**Route protection:**
- `/login` and `/register` check `isAuthenticated` — if true, redirect to `/dashboard`
- `/dashboard` is wrapped in `<ProtectedRoute>` — if not authenticated, redirect to `/login`

---

### 3.2 Auth State Management

#### `AuthContext.jsx`

Creates a React Context that provides auth state and methods to the entire app.

**State:**
| State | Type | Persistence |
|-------|------|-------------|
| `user` | Object | `localStorage.user` |
| `token` | String | `localStorage.accessToken` |
| `loading` | Boolean | Memory only |
| `error` | String | Memory only |

**Derived:** `isAuthenticated = !!token && !!user`

**On mount (`useEffect`):**
- If `accessToken` exists in localStorage, attempts `authAPI.refreshToken()`
- On success: updates token and user state
- On failure: silently clears all auth state (expired session)
- Sets `loading = false` when done

**Methods:**

| Method | What It Does |
|--------|-------------|
| `login(email, password)` | Calls `/auth/login`, stores token + user |
| `register(email, username, password)` | Calls `/auth/register` (doesn't log in) |
| `logout()` | Calls `/auth/logout`, clears everything |
| `handleOAuthCallback(accessToken)` | Stores token, tries refresh to get user data, falls back to JWT decoding |
| `updateProfile(updates)` | Calls `/users/update/{id}`, updates user state |
| `clearError()` | Resets error to null |

**OAuth callback detail:** When the frontend receives an access token from the OAuth redirect URL, it:
1. Stores the access token
2. Tries `refreshToken()` to get a full user object (the cookie was set by the backend)
3. If that fails, decodes the JWT payload (`atob(token.split('.')[1])`) to extract minimal user info

---

### 3.3 API Layer

#### `api.js`

Central Axios instance with two interceptors — the heart of frontend-backend communication.

**Instance setup:**
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8083/api/v1',
  withCredentials: true,              // sends cookies on every request
  headers: { 'Content-Type': 'application/json' }
});
```

**Request interceptor:**
Before every request, attaches `Authorization: Bearer <token>` from localStorage.

**Response interceptor (the clever part):**
When a request returns `401 Unauthorized`:

```
1. Is this already a retry? (_retry flag) → reject
2. Is someone else already refreshing? (isRefreshing flag)
   Yes → queue this request (Promise stored in failedQueue)
   No  → start refreshing:
     a. POST /auth/refresh-token (with credentials/cookies)
     b. Store new access token in localStorage
     c. Update axios default Authorization header
     d. Process the queue — retry all failed requests with new token
     e. Retry the original request
3. If refresh itself fails:
   a. Reject all queued requests
   b. Clear localStorage
   c. Redirect to /login
```

**Why the queue?** If 5 API calls fail simultaneously with 401, we don't want 5 refresh requests. The queue ensures only ONE refresh happens, and all 5 requests retry with the new token.

**Exported APIs:**

```javascript
authAPI: { register, login, logout, refreshToken }
userAPI: { getAll, getById, getByEmail, update, delete }
```

---

### 3.4 Pages

#### `HomePage.jsx` — Landing page (`/`)

Three sections:
1. **Hero** — Title, subtitle, CTA buttons (Get Started / Sign In OR Go to Dashboard), tech pills, decorative code card
2. **Features** — 6 feature cards (JWT Auth, OAuth2, Token Rotation, User Management, Security, Production Ready)
3. **CTA** — Bottom call-to-action card with GitHub link

**Conditional rendering:** Checks `isAuthenticated` to show either "Get Started" or "Go to Dashboard" buttons.

#### `LoginPage.jsx` — Login form (`/login`)

- Back button (← Home) top-left, ThemeToggle top-right
- Email + password inputs with client-side validation
- Submit → calls `login()` from AuthContext → navigates to `/dashboard`
- OAuth section: Google and GitHub buttons link to backend OAuth2 endpoints (`/oauth2/authorization/{provider}`)
- Handles `?error=oauth_failed` URL param (shown when OAuth is denied/fails)

#### `RegisterPage.jsx` — Registration form (`/register`)

- Same layout as Login (back button, theme toggle, glass card)
- 4 fields: email, username, password, confirm password
- Client-side validation: format, length, match
- On submit → calls `register()` → shows success alert → redirects to `/login` after 2s

#### `DashboardPage.jsx` — User profile (`/dashboard`)

Protected page (requires authentication). Shows:
- **Profile header card:** Avatar (from OAuth or letter placeholder), provider badge emoji (🔵 Google, ⚫ GitHub, 🔒 Local), join date, active status
- **Account details card:** Username + email (editable with Edit/Save/Cancel toggle)
- **Roles & Security card:** Role badges, account status, auth provider
- **Logout button** at the bottom

**Edit flow:** Click edit → fields become editable inputs → save calls `updateProfile()` → success message → fields go back to read-only.

#### `OAuthCallbackPage.jsx` — OAuth redirect handler (`/oauth/callback`)

Receives `?token=<jwt>` from the backend's OAuth redirect. Three states:
1. **Processing** — spinner, "Completing Sign In..."
2. **Success** — checkmark, redirects to `/dashboard` in 1.5s
3. **Error** — alert, redirects to `/login` in 3s

---

### 3.5 Reusable Components

#### `Navbar.jsx`

Conditionally hidden on auth pages (`/login`, `/register`, `/oauth/callback`).

**Guest state:** ThemeToggle + Sign In link + Sign Up link (gradient)
**Authenticated state:** ThemeToggle + user chip (avatar + username) → link to dashboard + logout button

#### `ProtectedRoute.jsx`

Wrapper component:
- If `loading` → shows spinner
- If not `isAuthenticated` → redirects to `/login`
- Otherwise → renders `{children}`

#### `Button.jsx`

Versatile button with:
- **Variants:** `primary` (gradient), `secondary` (outline), `ghost`, `danger`, `oauth`
- **Sizes:** `sm`, `md`, `lg`
- **States:** loading (shows `Loader2` spinner), disabled
- **Props:** optional `icon`, `fullWidth`

#### `Input.jsx`

Floating-label input:
- Label starts inside the input, animates up on focus or when value is present
- Left icon support
- Error message display below
- Focus ring with glow effect

#### `GlassCard.jsx`

Glassmorphism card with two optional effects:
- **`tilt`** — 3D rotation on mouse move (perspective 800px, ±10deg, 1.02 scale)
- **`glow`** — radial gradient glow that follows cursor position

#### `FloatingShapes.jsx`

Pure decoration — renders 3 animated spheres, 1 CSS 3D cube (6 faces), and 1 ring. All moved with CSS `@keyframes`. Marked `aria-hidden` for accessibility.

#### `ThemeToggle.jsx`

Button that toggles `data-theme` attribute on `<html>` between `"light"` and `"dark"`. Persists to `localStorage.theme`. Shows Sun/Moon icon from Lucide.

---

### 3.6 Styling & Theme System

#### `index.css` — Design Tokens

All colors, spacing, and effects are CSS custom properties (variables). Two complete palettes:

**Light mode (`:root`):**
- Primary: purple (`hsl(250, 85%, 60%)`)
- Accent: pink (`hsl(330, 85%, 60%)`)
- Gradient: purple → pink
- Surfaces: white/transparent
- Glass: white with blur

**Dark mode (`[data-theme="dark"]`):**
- Same hues, adjusted lightness
- Surfaces: dark gray/transparent
- Glass: dark with blur

**Shared tokens:** radius values, shadows, transitions, navbar height (72px).

**Global utilities:**
- `.gradient-text` — text with gradient fill
- `.animate-fadeInUp` — entrance animation
- `.delay-1` through `.delay-5` — staggered animation delays
- `@keyframes gradient-shift`, `pulse-glow`, etc.

**Each component/page has its own CSS file** — no CSS modules or CSS-in-JS, just BEM-style class names scoped by component prefix (e.g., `.navbar__logo`, `.home__hero-title`, `.auth-card__header`).

---

## 4. Authentication Flows

### 4.1 Registration Flow

```
User fills form → Client validates → POST /auth/register { email, username, password }
     │
     ▼
AuthController.register()
     │
     ▼
AuthServiceImpl.registerUser()
  ├─ Validates DTO not null
  ├─ BCrypt-encodes password
  └─ Calls UserService.createUser()
       ├─ Checks email uniqueness
       ├─ Maps DTO → Entity (provider = LOCAL)
       ├─ Saves to DB
       └─ Returns UserDto (no password)
     │
     ▼
201 Created → Frontend shows success → Redirects to /login
```

**Note:** Registration does NOT auto-login. User must log in separately.

### 4.2 Login Flow

```
User submits email + password
     │
     ▼
POST /auth/login { email, password }
     │
     ▼
AuthController.login()
  ├─ AuthenticationManager.authenticate() ← Spring Security verifies credentials
  │    └─ CustomUserDetailService.loadUserByUsername(email) ← loads user
  │    └─ BCrypt password comparison
  ├─ Checks user.isEnabled()
  ├─ Creates RefreshToken DB record (random UUID as jwtId)
  ├─ Generates access token (JWT, 1h, type=access, claims: email, roles)
  ├─ Generates refresh token (JWT, 24h, type=refresh, jti=jwtId)
  ├─ Sets refresh token as HttpOnly cookie
  └─ Returns TokenResponse { accessToken, refreshToken, tokenType, expiresIn, user }
     │
     ▼
Frontend stores accessToken in localStorage, user in localStorage
React state updates → app re-renders → redirect to /dashboard
```

### 4.3 Token Refresh Flow

```
Any API call returns 401
     │
     ▼
Axios response interceptor catches it
  ├─ If not already refreshing:
  │   POST /auth/refresh-token (withCredentials → browser sends cookie)
  │        │
  │        ▼
  │   AuthController.refreshToken()
  │     ├─ Reads refresh token (cookie > body > header)
  │     ├─ Verifies it's a refresh-type JWT
  │     ├─ Looks up RefreshToken in DB by jwtId
  │     ├─ Validates: correct user, not revoked, not expired
  │     ├─ REVOKES old token (revoked=true, replacedByToken=newJwtId)
  │     ├─ Creates NEW RefreshToken DB record
  │     ├─ Generates new access + refresh JWT pair
  │     ├─ Sets new refresh cookie
  │     └─ Returns new TokenResponse
  │        │
  │        ▼
  │   Frontend updates localStorage with new accessToken
  │   Retries the original failed request with new token
  │   Processes queued requests
  │
  └─ If already refreshing:
      Queue the request → will retry when refresh completes
```

### 4.4 OAuth2 Flow

```
User clicks "Google" or "GitHub" button
     │
     ▼
Browser navigates to: http://localhost:8083/oauth2/authorization/google
     │
     ▼
Spring Security redirects to Google's consent screen
     │
     ▼
User authorizes → Google redirects back to Spring with auth code
     │
     ▼
Spring exchanges code for tokens → gets user profile
     │
     ▼
OAuthAuthenticationSuccessHandler.onAuthenticationSuccess()
  ├─ Detects provider (Google vs GitHub)
  ├─ Extracts: email, name, providerId, avatar URL
  ├─ Searches for existing user by email
  │   ├─ Found → use existing user
  │   └─ Not found → create new user:
  │       ├─ Set provider, providerId, avatar
  │       ├─ No password (OAuth users don't have one)
  │       └─ Handle username conflict (append _google/_github)
  ├─ Create RefreshToken DB record
  ├─ Generate JWT access + refresh tokens
  ├─ Set refresh token as HttpOnly cookie
  └─ Redirect to: http://localhost:5173/oauth/callback?token=<accessToken>
     │
     ▼
OAuthCallbackPage.jsx
  ├─ Reads token from URL param
  ├─ Calls handleOAuthCallback(token)
  │   ├─ Stores token in localStorage
  │   ├─ Tries refreshToken() to get full user data
  │   └─ Fallback: decode JWT payload for minimal user info
  └─ Redirects to /dashboard
```

### 4.5 Logout Flow

```
User clicks logout
     │
     ▼
AuthContext.logout()
  ├─ POST /auth/logout (withCredentials → sends cookie)
  │        │
  │        ▼
  │   AuthController.logout()
  │     ├─ Reads refresh token from request
  │     ├─ If valid: revokes it in DB (revoked=true)
  │     ├─ Clears refresh token cookie (maxAge=0)
  │     ├─ Clears SecurityContext
  │     └─ Returns 204 No Content
  │
  ├─ Clears token state
  ├─ Clears user state
  ├─ Removes accessToken from localStorage
  ├─ Removes user from localStorage
  └─ Navigate to "/"
```

---

## 5. Data Model

```
┌────────────────────┐       ┌──────────────────────┐
│       users         │       │        roles          │
├────────────────────┤       ├──────────────────────┤
│ id          UUID PK │       │ id        UUID PK     │
│ email       VARCHAR │◄──┐  │ name      VARCHAR(50)  │
│ username    VARCHAR │   │  └──────────┬───────────┘
│ password    VARCHAR │   │             │
│ image       VARCHAR │   │  ┌──────────┴───────────┐
│ enabled     BOOLEAN │   │  │     user_roles         │
│ provider    VARCHAR │   │  ├──────────────────────┤
│ provider_id VARCHAR │   │  │ user_id    UUID FK ──►│
│ created_at  TIMESTAMP│   └──│ role_id    UUID FK ──►│
│ updated_at  TIMESTAMP│      └──────────────────────┘
└────────┬───────────┘
         │
         │ 1:N
         ▼
┌──────────────────────────┐
│     refresh_tokens        │
├──────────────────────────┤
│ id               UUID PK  │
│ jwt_id           VARCHAR   │ ← unique index
│ user_id          UUID FK   │ ← indexed
│ created_at       TIMESTAMP │
│ expires_at       TIMESTAMP │
│ revoked          BOOLEAN   │
│ replaced_by_token VARCHAR  │ ← chain to next token
└──────────────────────────┘
```

`spring.jpa.hibernate.ddl-auto=update` means Hibernate auto-creates/updates these tables on startup.

---

## 6. Security Architecture

| Layer | Mechanism | Purpose |
|-------|-----------|---------|
| **Password storage** | BCrypt hash | Irreversible — even if DB is leaked, passwords are safe |
| **Token signing** | HMAC-SHA512 | Tamper-proof tokens — changing a single character invalidates them |
| **Access token** | Short-lived (1h) | Limits damage window if stolen |
| **Refresh token** | HTTP-only cookie | Cannot be read by JavaScript (XSS immune) |
| **Cookie flags** | SameSite=Lax, Secure-ready | Prevents CSRF attacks, enforces HTTPS in production |
| **Token rotation** | New token on every refresh | Stolen refresh tokens become useless after next rotation |
| **Revocation chain** | `replacedByToken` in DB | Enables detection of token theft (if old token reused after rotation) |
| **CORS** | Whitelisted origins only | Blocks requests from unauthorized domains |
| **Stateless sessions** | No server-side session | Eliminates session fixation/hijacking attacks |
| **Input validation** | `@Valid` + annotations | Prevents injection and malformed data |
| **Error sanitization** | `GlobalExceptionHandler` | Never exposes internal details or stack traces |
| **OAuth account safety** | Username conflict resolution | Appends provider suffix to prevent account hijacking |

---

## 7. Environment & Configuration

### Config Hierarchy

```
application.properties          ← base (app name, port, active profile)
  └── application-dev.properties    ← dev overrides (DB, JWT, OAuth, CORS)
  └── application-qa.properties     ← QA overrides (empty — configure as needed)
  └── application-prod.properties   ← production overrides (empty — configure as needed)
```

### All Environment Variables

| Variable | Default | Used In |
|----------|---------|---------|
| `DB_USER` | *(required)* | Database username |
| `DB_PASS` | *(required)* | Database password |
| `JWT_SECRET` | 64-char default | Token signing key |
| `JWT_ISSUER` | `auth-app` | JWT issuer claim |
| `JWT_ACCESS_TTL_SECONDS` | `3600` | Access token lifetime |
| `JWT_REFRESH_TTL_SECONDS` | `86400` | Refresh token lifetime |
| `JWT_REFRESH_COOKIE_NAME` | `refreshToken` | Cookie name |
| `JWT_COOKIE_SECURE` | `false` | Cookie secure flag (set `true` in prod) |
| `JWT_COOKIE_HTTP_ONLY` | `true` | Cookie HTTP-only flag |
| `JWT_COOKIE_SAME_SITE` | `lax` | Cookie SameSite policy |
| `JWT_COOKIE_DOMAIN` | `localhost` | Cookie domain |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed CORS origins |
| `FRONTEND_URL` | `http://localhost:5173` | OAuth redirect base URL |
| `GOOGLE_CLIENT_ID` | *(required for OAuth)* | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | *(required for OAuth)* | Google OAuth2 secret |
| `GITHUB_CLIENT_ID` | *(required for OAuth)* | GitHub OAuth2 client ID |
| `GITHUB_CLIENT_SECRET` | *(required for OAuth)* | GitHub OAuth2 secret |

---

## 8. File-by-File Reference

### Backend — `Auth-App-Backened/`

| File | Purpose | Key Details |
|------|---------|-------------|
| `pom.xml` | Maven dependencies | Spring Boot 3.5.7, Java 25, JJWT 0.13, SpringDoc 2.8 |
| `AuthAppBackenedApplication.java` | Entry point | `@SpringBootApplication` |
| **configs/** | | |
| `ApiConstant.java` | URL whitelist | Array of public endpoint patterns |
| `SecurityConfig.java` | Security filter chain | CSRF off, stateless, JWT filter, OAuth2, public endpoints |
| `CorsConfig.java` | CORS rules | Origins from env, credentials enabled |
| `OAuthAuthenticationSuccessHandler.java` | OAuth2 callback | Creates user, generates tokens, redirects to frontend |
| `MapperConfig.java` | ModelMapper bean | Custom field mappings |
| `OpenAPIConfig.java` | Swagger config | API docs metadata, Bearer scheme |
| **entities/** | | |
| `User.java` | User entity/table | Implements `UserDetails`, UUID PK, ManyToMany roles |
| `Role.java` | Role entity/table | UUID PK, unique name |
| `RefreshToken.java` | Token tracking table | JTI, expiration, revocation, rotation chain |
| `enums/Provider.java` | Auth provider enum | LOCAL, GOOGLE, GITHUB |
| `enums/LoginRequest.java` | Login payload | Record: email, password |
| `enums/TokenResponse.java` | Token response | Record: tokens + user DTO |
| `enums/ErrorResponse.java` | Error payload | Record: message, status, code |
| **dtos/** | | |
| `UserDto.java` | User transfer object | Password write-only, Builder pattern |
| `RoleDto.java` | Role transfer object | id + name |
| **repositories/** | | |
| `UserRepo.java` | User data access | findByEmail, existsByEmail, findByUsername |
| `RefreshTokenRepo.java` | Token data access | findByJwtId, deleteByUserId |
| **services/** | | |
| `AuthService.java` | Auth interface | registerUser() |
| `AuthServiceImpl.java` | Auth implementation | BCrypt encoding + delegation |
| `UserService.java` | User interface | Full CRUD |
| `UserServiceImpl.java` | User implementation | Validation, selective updates, cascading delete |
| **jwt/** | | |
| `JwtService.java` | Token engine | Generate, parse, validate access/refresh tokens |
| `JwtAuthenticationFilter.java` | Request filter | Extracts Bearer token, sets SecurityContext |
| `CookieService.java` | Cookie management | Set/clear refresh token cookie |
| `CustomUserDetailService.java` | UserDetailsService | Loads user by email for Spring Security |
| **exceptions/** | | |
| `ResourceNotFoundException.java` | 404 exception | Custom runtime exception |
| `GlobalExceptionHandler.java` | Error handler | Maps exceptions to JSON responses |
| **controllers/** | | |
| `AuthController.java` | Auth API | login, register, logout, refresh |
| `UserController.java` | User API | CRUD endpoints |

### Frontend — `Auth-App-Frontend/`

| File | Purpose | Key Details |
|------|---------|-------------|
| `index.html` | HTML shell | Favicon, meta tags, root div |
| `vite.config.js` | Build config | React plugin |
| `package.json` | Dependencies | React 19, Vite 7, Axios, Lucide |
| `main.jsx` | React entry | Renders App into #root |
| `App.jsx` | App shell | BrowserRouter, AuthProvider, routes |
| `index.css` | Design tokens | CSS variables, light/dark themes, animations |
| `App.css` | App-level styles | Minimal wrapper styles |
| **context/** | | |
| `AuthContext.jsx` | Auth state | Provider with login/register/logout/refresh/OAuth |
| **services/** | | |
| `api.js` | HTTP client | Axios instance, request/response interceptors, token refresh queue |
| **pages/** | | |
| `HomePage.jsx` | Landing | Hero, features, CTA, footer |
| `LoginPage.jsx` | Login | Form, validation, OAuth buttons |
| `RegisterPage.jsx` | Register | Form, validation, success redirect |
| `DashboardPage.jsx` | Profile | User info, edit, roles, logout |
| `OAuthCallbackPage.jsx` | OAuth handler | Token processing, redirect |
| **components/** | | |
| `Navbar.jsx` | Navigation | Logo, theme, auth links/user chip |
| `ProtectedRoute.jsx` | Route guard | Auth check, loading, redirect |
| `Button.jsx` | Button | Variants, sizes, loading, icons |
| `Input.jsx` | Input | Floating label, icon, error |
| `GlassCard.jsx` | Card | Glass effect, 3D tilt, cursor glow |
| `FloatingShapes.jsx` | Background | Animated decorative shapes |
| `ThemeToggle.jsx` | Theme switch | Light/dark, localStorage persist |

---

> **This document should give you (or any new developer) a complete understanding of every
> piece of the AuthApp codebase — how it works, why it's built that way, and how all the
> parts connect.**
