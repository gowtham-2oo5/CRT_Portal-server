# CRT Portal Core Server

## Overview
CRT Portal Core Server is a Spring Boot-based backend service that provides authentication, user management, and role-based access control for the CRT Portal application. The service implements JWT-based authentication with OTP verification and supports MySQL as its primary database.

## Technical Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Authentication**: JWT + OTP
- **Build Tool**: Maven 3.6+
- **API Documentation**: Swagger/OpenAPI 3.0

## Architecture

### Core Components
1. **Authentication Module**
   - JWT-based authentication
   - OTP verification via email
   - Refresh token mechanism
   - Password reset functionality

2. **User Management**
   - Role-based access control (ADMIN, FACULTY)
   - User CRUD operations
   - Profile management

3. **Security Layer**
   - JWT token validation
   - Role-based authorization
   - CORS configuration
   - Password encryption (BCrypt)

### Database Schema
```sql
-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'FACULTY') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Development Setup

### Prerequisites
1. **Java Development Kit**
   ```bash
   # Verify Java installation
   java -version
   # Should show Java 17 or higher
   ```

2. **MySQL Server**
   ```bash
   # Verify MySQL installation
   mysql --version
   # Should show MySQL 8.0 or higher
   ```

3. **Maven**
   ```bash
   # Verify Maven installation
   mvn --version
   # Should show Maven 3.6 or higher
   ```

### Environment Configuration

1. **Database Setup**
   ```sql
   -- Create database
   CREATE DATABASE IF NOT EXISTS crt_portal;
   
   -- Create application user
   CREATE USER 'crt_user'@'localhost' IDENTIFIED BY 'your_secure_password';
   GRANT ALL PRIVILEGES ON crt_portal.* TO 'crt_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Environment Variables**
   Create `.env` file from template:
   ```bash
   cp env.template.txt .env
   ```
   
   Required environment variables:
   ```properties
   # Database
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=crt_portal
   DB_USERNAME=crt_user
   DB_PASSWORD=your_secure_password

   # JWT
   JWT_SECRET=your_very_long_and_secure_jwt_secret_key_at_least_256_bits
   JWT_EXPIRATION=86400000
   JWT_REFRESH_EXPIRATION=604800000

   # Email (Gmail)
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USERNAME=your_email@gmail.com
   SMTP_PASSWORD=your_app_specific_password
   SMTP_FROM=your_email@gmail.com
   ```

### Building and Running

1. **Build the Application**
   ```bash
   mvn clean install
   ```

2. **Run in Development Mode**
   ```bash
   mvn spring-boot:run
   ```

3. **Run Tests**
   ```bash
   mvn test
   ```

## API Documentation

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
    "usernameOrEmail": "string",
    "password": "string"
}
```

#### OTP Verification
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
    "usernameOrEmail": "string",
    "otp": "string"
}
```

#### Refresh Token
```http
POST /api/auth/refresh-token
Authorization: Bearer <refresh_token>
```

### User Management Endpoints

#### Create User (Admin Only)
```http
POST /api/admin/users
Authorization: Bearer <token>
Content-Type: application/json

{
    "name": "string",
    "email": "string",
    "phone": "string",
    "username": "string",
    "password": "string",
    "role": "ADMIN|FACULTY"
}
```

## Security Implementation

### JWT Token Structure
```json
{
    "sub": "username",
    "userId": "uuid",
    "name": "string",
    "email": "string",
    "role": "ADMIN|FACULTY",
    "iat": "timestamp",
    "exp": "timestamp"
}
```

### Role-Based Access Control
- **ADMIN**: Full system access
- **FACULTY**: Limited access to specific resources

### Security Headers
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .headers(headers -> headers
            .xssProtection(xss -> {})
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'")
            )
        )
        .build();
}
```

## Development Guidelines

### Code Style
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add Javadoc for public methods
- Keep methods focused and small

### Testing
- Write unit tests for service layer
- Integration tests for controllers
- Test security configurations
- Mock external services

### Logging
- Use appropriate log levels
- Include request IDs in logs
- Log security events
- Avoid logging sensitive data

## Production Deployment

### Database Configuration
1. Use dedicated database user
2. Enable SSL connections
3. Configure connection pool
4. Set up regular backups

### Security Checklist
1. Enable HTTPS
2. Configure CORS properly
3. Set secure JWT secret
4. Implement rate limiting
5. Regular security audits

### Monitoring
1. Application metrics
2. Database performance
3. Security events
4. Error rates

## Troubleshooting

### Common Issues
1. **Database Connection**
   ```bash
   # Check MySQL service
   sudo systemctl status mysql
   
   # Verify connection
   mysql -u crt_user -p crt_portal
   ```

2. **JWT Issues**
   - Verify token expiration
   - Check JWT secret
   - Validate token structure

3. **Email Configuration**
   - Verify SMTP settings
   - Check Gmail app password
   - Test email sending

## Contributing
1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create pull request

## Support
[You can find contact options](https://github.com/gowtham-2oo5)