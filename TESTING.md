# 🧪 AuthApp — Complete Testing Checklist

> **Purpose**: Step-by-step manual QA checklist to verify every feature of the AuthApp project.
> Work through each section in order. Check ☑ each item as it passes.

---

## 📋 Pre-requisites

Before testing, ensure the following are running:

| Service | Command | Expected |
|---------|---------|----------|
| PostgreSQL | `psql -U postgres` | Connected, `auth-app-db` exists |
| Backend | `cd Auth-App-Backened && ./mvnw spring-boot:run` | Starts on `http://localhost:8083` |
| Frontend | `cd Auth-App-Frontend && npm run dev` | Starts on `http://localhost:5173` |

Set these environment variables before starting the backend:
```
DB_USER, DB_PASS, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET
```

---

## 1. 🏠 Home Page (`/`)

### 1.1 Layout & Rendering
- [ ] Page loads without console errors
- [ ] Navbar is visible at the top with "AuthApp" logo (Shield icon + text)
- [ ] ThemeToggle button visible in navbar
- [ ] Floating animated shapes render in background
- [ ] Gradient mesh background is visible behind content

### 1.2 Hero Section
- [ ] Title "Secure Authentication, Made Simple" renders with gradient text
- [ ] Subtitle paragraph is visible below the title
- [ ] "Open-source & self-hosted" badge shows with green checkmark
- [ ] "Get Started" (primary) and "Sign In" (secondary) buttons are visible (when NOT logged in)
- [ ] Tech stack pills render: Spring Boot 3, Spring Security, PostgreSQL, React 19, JWT (JJWT), OAuth2 Client, Vite, Axios
- [ ] Decorative code card on right side shows a mock POST `/api/v1/auth/login` request/response

### 1.3 Features Section
- [ ] Section title "Everything You Need to Ship Auth Fast" renders
- [ ] 6 feature cards render: JWT Authentication, OAuth2 Social Login, Token Rotation, User Management, Secure by Default, Production Ready
- [ ] Each card has an icon, title, and description
- [ ] Cards have 3D tilt effect on hover (mouse move)

### 1.4 CTA Section
- [ ] "Ready to Get Started?" card renders with shield icon
- [ ] "Create Free Account" and "View Source" buttons visible (when NOT logged in)
- [ ] "View Source" opens GitHub link in new tab

### 1.5 Footer
- [ ] Footer text "Built with ❤️ by Lakshay · Spring Boot & React" is visible

### 1.6 Navbar — Guest State
- [ ] "Sign In" link (with LogIn icon) is visible
- [ ] "Sign Up" link (with UserPlus icon, gradient background) is visible
- [ ] Sign Up button looks clean (no oversized glow/shadow)
- [ ] Clicking "Sign In" navigates to `/login`
- [ ] Clicking "Sign Up" navigates to `/register`

### 1.7 Responsive
- [ ] At `≤600px`, navbar auth link text hides (icons only)
- [ ] Hero section stacks vertically on mobile (`≤768px`)
- [ ] Feature cards stack into fewer columns on smaller screens

---

## 2. 🌗 Theme Toggle

- [ ] Click ThemeToggle in navbar → page switches to dark mode
- [ ] Colors (background, surfaces, text, borders) all update correctly
- [ ] Click again → switches back to light mode
- [ ] Refresh the page → theme persists (stored in `localStorage` key: `theme`)
- [ ] Icon shows Moon (☽) in light mode, Sun (☀) in dark mode

---

## 3. 📝 Register Page (`/register`)

### 3.1 Page Layout
- [ ] Back button "← Home" is visible at **top-left** corner
- [ ] ThemeToggle is visible at **top-right** corner
- [ ] Floating shapes animate in background
- [ ] Glass card renders centered with Shield logo, title "Create Account", subtitle
- [ ] 4 input fields: Email, Username, Password, Confirm Password
- [ ] "Create Account" button at bottom
- [ ] Footer: "Already have an account? Sign in" link

### 3.2 Navigation
- [ ] Clicking "← Home" navigates back to `/`
- [ ] Clicking "Sign in" link navigates to `/login`
- [ ] Navbar is **hidden** on this page

### 3.3 Client-Side Validation
- [ ] Submit empty form → "Email is required" error shows on email field
- [ ] Enter invalid email (e.g. `abc`) → "Invalid email format" error
- [ ] Enter username less than 3 chars → "At least 3 characters" error
- [ ] Enter password less than 6 chars → "At least 6 characters" error
- [ ] Enter mismatched passwords → "Passwords do not match" error
- [ ] Errors clear when user starts typing in the errored field

### 3.4 Successful Registration
- [ ] Fill all fields correctly → Click "Create Account"
- [ ] Button shows loading spinner while request is pending
- [ ] Green success alert appears: "Account created! Redirecting to login..."
- [ ] After ~2 seconds, page redirects to `/login`
- [ ] Check database: new user exists in `users` table with BCrypt-hashed password, `provider = LOCAL`

### 3.5 Registration Errors
- [ ] Register with an already-used email → Red error alert with server message (e.g. "Email already exists")
- [ ] Register with an already-used username → appropriate error message

---

## 4. 🔐 Login Page (`/login`)

### 4.1 Page Layout
- [ ] Back button "← Home" visible at top-left
- [ ] ThemeToggle at top-right
- [ ] Glass card with Shield logo, "Welcome Back" title, "Sign in to continue" subtitle
- [ ] Email and Password input fields
- [ ] "Sign In" submit button
- [ ] Divider: "OR CONTINUE WITH"
- [ ] Google and GitHub OAuth buttons
- [ ] Footer: "Don't have an account? Create one" link

### 4.2 Navigation
- [ ] "← Home" → navigates to `/`
- [ ] "Create one" link → navigates to `/register`
- [ ] Navbar is **hidden** on this page

### 4.3 Client-Side Validation
- [ ] Submit empty → "Email is required" and "Password is required" errors
- [ ] Invalid email format → "Invalid email format" error

### 4.4 Successful Login
- [ ] Enter valid credentials → Click "Sign In"
- [ ] Button shows loading spinner
- [ ] On success, redirects to `/dashboard`
- [ ] Check `localStorage`: `accessToken` is set (JWT string), `user` is set (JSON object)
- [ ] Check browser cookies: `refreshToken` cookie exists (HttpOnly — may not be visible in DevTools Application tab, but check in Network tab Set-Cookie header)

### 4.5 Login Errors
- [ ] Wrong password → Red error alert: "Invalid credentials" or similar
- [ ] Non-existent email → Error alert
- [ ] Disabled user account → Error about account not enabled

### 4.6 Redirect If Authenticated
- [ ] While logged in, navigate to `/login` directly → should redirect to `/dashboard`
- [ ] Same for `/register` → redirects to `/dashboard`

---

## 5. 🔑 OAuth2 Login (Google)

### 5.1 Google OAuth Flow
- [ ] On Login page, click "Google" button
- [ ] Browser redirects to `http://localhost:8083/oauth2/authorization/google`
- [ ] Google consent screen appears
- [ ] Authorize → redirected to `http://localhost:5173/oauth/callback?token=<jwt>`
- [ ] OAuth callback page shows "Completing Sign In..." with spinner
- [ ] Status changes to "Welcome!" with checkmark
- [ ] Redirects to `/dashboard` after ~1.5s
- [ ] Dashboard shows correct user info (name, email from Google, provider badge 🔵)
- [ ] Check database: user exists with `provider = GOOGLE`, `providerId` set, `image` set to Google avatar URL

### 5.2 Google OAuth — Returning User
- [ ] Log out, then sign in with Google again
- [ ] Should find existing user (no duplicate created)
- [ ] Dashboard shows same user with updated data if Google profile changed

---

## 6. 🔑 OAuth2 Login (GitHub)

### 6.1 GitHub OAuth Flow
- [ ] On Login page, click "GitHub" button
- [ ] Browser redirects to `http://localhost:8083/oauth2/authorization/github`
- [ ] GitHub authorization page appears
- [ ] Authorize → redirected to callback with token
- [ ] Same flow as Google: spinner → success → dashboard
- [ ] Dashboard shows provider badge ⚫, correct GitHub username/email
- [ ] Check database: `provider = GITHUB`, `providerId` = GitHub numeric ID

### 6.2 OAuth Error Handling
- [ ] Cancel/deny OAuth consent → should redirect to login page with `?error=oauth_failed`
- [ ] Login page shows error: "OAuth login failed. Please try again or use email/password."

---

## 7. 📊 Dashboard Page (`/dashboard`)

### 7.1 Page Layout
- [ ] Navbar visible with user avatar chip (image or initial letter), username, logout button
- [ ] Profile header card: avatar (or placeholder with initial), provider badge, join date, status (Active/Inactive)
- [ ] Account details card: Username and Email displayed
- [ ] Roles & Security card: role badges, account status, auth provider shown

### 7.2 Profile Editing
- [ ] Click "Edit Profile" button → input fields become editable, Save & Cancel buttons appear
- [ ] Change username → Click Save
- [ ] Loading spinner on Save button while request in progress
- [ ] Success message: "Profile updated successfully!" (disappears after 3s)
- [ ] Username updates in the profile header AND in the navbar chip
- [ ] Cancel editing → reverts to original values without API call

### 7.3 Profile Edit Errors
- [ ] Try to update with an already-taken email → error message from server
- [ ] Try to update with an already-taken username → error message from server

### 7.4 Logout from Dashboard
- [ ] Click logout button (LogOut icon in navbar OR "Sign Out" button at bottom)
- [ ] Redirects to `/`
- [ ] `localStorage` cleared: no `accessToken`, no `user`
- [ ] Refresh token cookie cleared (check Network tab)
- [ ] Navigating to `/dashboard` now redirects to `/login`
- [ ] Check database: refresh token record has `revoked = true`

---

## 8. 🔄 Token Refresh Flow

### 8.1 Automatic Refresh on 401
- [ ] Log in successfully
- [ ] In browser DevTools → Application → Local Storage → manually delete `accessToken`
- [ ] Trigger any API call (e.g., edit profile)
- [ ] Observe Network tab: first request gets 401, then `POST /auth/refresh-token` fires, then original request retries with new token
- [ ] `localStorage.accessToken` is updated to the new token
- [ ] Operation completes successfully without user seeing any error

### 8.2 Refresh on Page Reload
- [ ] Log in → reload the page
- [ ] `POST /auth/refresh-token` fires on mount (visible in Network tab)
- [ ] User stays logged in with fresh tokens
- [ ] Dashboard data loads correctly

### 8.3 Refresh Token Rotation
- [ ] Before refresh: note the `jwtId` in `refresh_tokens` table
- [ ] Trigger a refresh (reload page or wait for 401)
- [ ] After refresh: old record has `revoked = true` and `replaced_by_token = <new_jti>`
- [ ] New record exists with `revoked = false` and a new `jwt_id`

### 8.4 Expired Refresh Token
- [ ] In the database, manually set a refresh token's `expires_at` to a past timestamp
- [ ] Clear `accessToken` from localStorage
- [ ] Trigger any API call → refresh attempt fails
- [ ] User is redirected to `/login`
- [ ] `localStorage` is cleared

### 8.5 Revoked Refresh Token
- [ ] In the database, manually set `revoked = true` on the active refresh token
- [ ] Clear `accessToken` from localStorage
- [ ] Trigger any API call → refresh fails → redirect to `/login`

---

## 9. 🛡️ Protected Routes

- [ ] Clear all auth state (localStorage + cookies) → go to `/dashboard` → redirected to `/login`
- [ ] Shows loading spinner briefly during auth check, then redirects
- [ ] Directly navigating to unknown route (e.g. `/xyz`) → redirects to `/`

---

## 10. 📡 Backend API — Direct Testing (Swagger / cURL)

Open **http://localhost:8083/swagger-ui.html**

### 10.1 Swagger UI
- [ ] Swagger page loads with title "Auth App Backend API"
- [ ] All endpoints listed under Auth Controller and User Controller
- [ ] "Authorize" button available for Bearer token input

### 10.2 POST `/api/v1/auth/register`
```json
{
  "email": "test@test.com",
  "username": "testuser",
  "password": "password123"
}
```
- [ ] Returns `201 Created` with UserDto (no password in response)
- [ ] Duplicate email → returns `400` with error message
- [ ] Missing fields → returns appropriate validation error

### 10.3 POST `/api/v1/auth/login`
```json
{
  "email": "test@test.com",
  "password": "password123"
}
```
- [ ] Returns `200 OK` with `accessToken`, `refreshToken`, `tokenType: "Bearer"`, `expiresIn: 3600`, and `user` object
- [ ] Response has `Set-Cookie` header with `refreshToken` (HttpOnly)
- [ ] Wrong password → `401 Unauthorized` with "Invalid credentials"

### 10.4 POST `/api/v1/auth/refresh-token`
- [ ] Send with the refresh token cookie → `200 OK` with new token pair
- [ ] Send with `X-Refresh-Token` header → works
- [ ] Send with refresh token in body → works
- [ ] Send expired/revoked/missing token → `401`
- [ ] Old token is marked `revoked` in database

### 10.5 POST `/api/v1/auth/logout`
- [ ] Send with valid refresh token → `204 No Content`
- [ ] `Set-Cookie` header clears the refresh token
- [ ] Token marked as revoked in database
- [ ] Send without any token → still returns `204` (graceful)

### 10.6 User CRUD (requires Bearer token in Authorization header)

**GET `/api/v1/users/getAll`**
- [ ] Returns list of all users
- [ ] Without token → `401`

**GET `/api/v1/users/getById/{id}`**
- [ ] Valid UUID → user returned
- [ ] Non-existent UUID → `404 Not Found`

**GET `/api/v1/users/getByEmail/{email}`**
- [ ] Valid email → user returned
- [ ] Non-existent email → `404`

**PUT `/api/v1/users/update/{id}`**
```json
{
  "username": "updatedName",
  "email": "updated@test.com"
}
```
- [ ] Returns updated user
- [ ] Password field is not exposed in response
- [ ] Duplicate email/username → appropriate error

**DELETE `/api/v1/users/delete/{id}`**
- [ ] Returns `204 No Content`
- [ ] User no longer in database
- [ ] Associated refresh tokens also deleted
- [ ] Non-existent ID → `404`

---

## 11. 🍪 Cookie Behavior

- [ ] After login, check `Set-Cookie` header: `refreshToken=eyJ...; Path=/; HttpOnly; SameSite=Lax`
- [ ] Cookie domain is `localhost` in dev
- [ ] Cookie is NOT visible via `document.cookie` in browser console (HttpOnly)
- [ ] After logout, cookie is cleared (maxAge=0 in Set-Cookie)
- [ ] After refresh, old cookie replaced with new one

---

## 12. 🔐 Security Checks

### 12.1 JWT Validation
- [ ] Tamper with an access token (change a character) → API returns `401`
- [ ] Use a refresh token as an access token in Authorization header → rejected (type check)
- [ ] Use an access token for refresh endpoint → rejected

### 12.2 CORS
- [ ] API responds with `Access-Control-Allow-Origin: http://localhost:5173`
- [ ] API from different origin (e.g., `http://localhost:3000`) → CORS error
- [ ] Preflight OPTIONS requests return correct CORS headers

### 12.3 No Stack Traces Leaked
- [ ] Trigger a `500` error (e.g., DB down) → response is `{ "message": "...", "status": "...", "code": 500 }`
- [ ] No Java stack traces in API response body

---

## 13. 🎨 UI/UX Polish

- [ ] All animations play on page load (fadeInUp with staggered delays)
- [ ] GlassCard tilt effect works on feature cards (move mouse around)
- [ ] GlassCard glow follows cursor on glowing cards
- [ ] Input fields have floating label animation (label moves up on focus)
- [ ] Button hover effects work: primary buttons lift + glow, secondary buttons lift + border change
- [ ] Loading spinners animate (rotate) on buttons during API calls
- [ ] Error alerts have red background, success alerts have green background
- [ ] Favicon in browser tab is the Shield logo (not default Vite logo)

---

## 14. 🔀 Edge Cases

- [ ] Double-click submit button → only one API request sent (button disabled during loading)
- [ ] Rapid navigation between pages → no crashes or stale state
- [ ] Open app in two tabs → log out in one → other tab's next API call triggers refresh failure → redirected to login
- [ ] Very long username (64 chars) → truncated gracefully in navbar chip
- [ ] Very long email in dashboard → doesn't break layout
- [ ] Browser back button from dashboard after logout → lands on home, not dashboard

---

## 15. ✅ Final Smoke Test

Perform this complete flow without stopping:

1. [ ] Open `http://localhost:5173` → Home page loads
2. [ ] Toggle theme to dark → all elements styled correctly
3. [ ] Click "Get Started" → Register page
4. [ ] Register with `smoke@test.com` / `smokeuser` / `Test1234`
5. [ ] Redirected to login → success message shown
6. [ ] Log in with `smoke@test.com` / `Test1234`
7. [ ] Dashboard loads with correct info
8. [ ] Edit username to `smokeuser_updated` → save → success
9. [ ] Navbar chip shows updated name
10. [ ] Refresh page → still logged in (token refreshed)
11. [ ] Log out → back to home page
12. [ ] Try to visit `/dashboard` → redirected to `/login`
13. [ ] Log in with Google OAuth → dashboard shows Google info
14. [ ] Log out
15. [ ] Log in with GitHub OAuth → dashboard shows GitHub info
16. [ ] Log out → all clear ✅

---

> **Total test cases: ~130+**
> 
> Estimated manual testing time: **45–60 minutes**
>
> Mark each checkbox as you go. If any test fails, note the step number and error for debugging.
