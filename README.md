# Spring Security JWT Auth Service 

A practice for Spring Security & JWT 

Based on [Spring Boot JWT Authentication example with Spring Security & Spring Data JPA](https://github.com/bezkoder/spring-boot-spring-security-jwt-authentication?tab=readme-ov-file#spring-boot-jwt-authentication-example-with-spring-security--spring-data-jpa)


## New features
- Reset password via email
- Get/Edit user information 

## TODO
[] Login with Google/Github

## API Documentation

### Authentication APIs (`/api/auth`)

#### User Registration
- **POST** `/api/auth/signup`
- Description: Register a new user
- Request body: username, email, password, avatarUrl (optional)
- Example:
```bash
curl -X POST http://localhost:7073/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "avatarUrl": "https://example.com/avatar.jpg"
  }'
```

#### User Login
- **POST** `/api/auth/signin`
- Description: Authenticate user and get JWT token
- Request body: username, password
- Example:
```bash
curl -X POST http://localhost:7073/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### Forgot Password
- **POST** `/api/auth/forgot-password`
- Description: Send password reset verification code to email
- Request body: email
- Example:
```bash
curl -X POST http://localhost:7073/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

#### Reset Password
- **POST** `/api/auth/reset-password`
- Description: Reset password using verification code
- Request body: email, code, newPassword
- Example:
```bash
curl -X POST http://localhost:7073/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "123456",
    "newPassword": "newpassword123"
  }'
```

### User APIs (`/api/user`)

#### Update User Profile
- **PUT** `/api/user/profile`
- Description: Update user profile information
- Request body: username, email, avatarUrl (all optional)
- Authentication: Required (JWT token)
- Example:
```bash
curl -X PUT http://localhost:7073/api/user/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "username": "newusername",
    "email": "newemail@example.com",
    "avatarUrl": "https://example.com/new-avatar.jpg"
  }'
```


