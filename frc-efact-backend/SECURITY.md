# FRC eFact Backend - Security Configuration

## Overview

This document describes the security measures implemented in the FRC eFact backend application.

## HTTPS/SSL Configuration

### SSL Termination

Render handles SSL termination at the load balancer level:
- Automatic SSL certificates via Let's Encrypt
- TLS 1.2+ support
- Automatic certificate renewal
- HTTPS redirect handled by Render

### HTTPS Enforcement

The application enforces HTTPS in production:

```java
.requiresChannel(channel -> channel
    .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
    .requiresSecure())
```

This configuration:
- Detects proxy headers from Render
- Requires secure channel (HTTPS) for all requests
- Works with Render's SSL termination

## Security Headers

### Implemented Headers

| Header | Value | Purpose |
|--------|-------|---------|
| X-Frame-Options | DENY | Prevents clickjacking attacks |
| X-Content-Type-Options | nosniff | Prevents MIME type sniffing |
| X-XSS-Protection | 1; mode=block | Enables XSS filter in browsers |
| Strict-Transport-Security | max-age=31536000; includeSubDomains; preload | Forces HTTPS for 1 year |
| Content-Security-Policy | See below | Restricts resource loading |
| Referrer-Policy | strict-origin-when-cross-origin | Controls referrer information |
| Permissions-Policy | geolocation=(), microphone=(), camera=() | Restricts browser features |

### Content Security Policy (CSP)

```
default-src 'self';
script-src 'self' 'unsafe-inline' 'unsafe-eval';
style-src 'self' 'unsafe-inline';
img-src 'self' data: https:;
font-src 'self' data:;
connect-src 'self' https://frc-efact-backend.onrender.com
```

**Note**: `unsafe-inline` and `unsafe-eval` are required for Angular Material. Consider using nonces in future versions.

## CORS Configuration

### Allowed Origins

Production:
```yaml
cors:
  allowed-origins:
    - https://frc-efact-frontend.onrender.com
```

Development:
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:4200",
    "https://localhost:4200",
    "https://*.onrender.com"
));
```

### CORS Settings

- **Methods**: GET, POST, PUT, DELETE, OPTIONS, PATCH
- **Headers**: All headers allowed (*)
- **Credentials**: Enabled (true)
- **Exposed Headers**: Authorization
- **Max Age**: 3600 seconds (1 hour)

### Updating CORS for Custom Domain

If using a custom domain, update `application-prod.yml`:

```yaml
cors:
  allowed-origins:
    - https://your-custom-domain.com
    - https://frc-efact-frontend.onrender.com
```

## JWT Token Security

### Token Configuration

```yaml
jwt:
  secret: ${JWT_SECRET}  # Minimum 64 characters
  expiration-ms: 86400000  # 24 hours
  refresh-expiration-ms: 604800000  # 7 days
```

### Token Storage

**Current Implementation**: Tokens stored in memory (frontend)
- ✅ Not vulnerable to XSS attacks on localStorage
- ✅ Cleared on browser close
- ⚠️ Lost on page refresh (requires re-login)

**Future Enhancement**: HTTP-only cookies
```java
public ResponseCookie createJwtCookie(String token) {
    return ResponseCookie.from("jwt", token)
        .httpOnly(true)           // Prevents JavaScript access
        .secure(true)             // HTTPS only
        .sameSite("Strict")       // CSRF protection
        .maxAge(Duration.ofHours(24))
        .path("/")
        .build();
}
```

### Token Validation

- Signature verification using HMAC-SHA512
- Expiration check on every request
- User ID and username claims validation
- Refresh token type verification

## Authentication Security

### Password Security

- **Algorithm**: BCrypt with default strength (10 rounds)
- **Salt**: Automatically generated per password
- **Storage**: Only hashed passwords stored in database

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Rate Limiting

Rate limiting implemented for authentication endpoints:
- **Limit**: 5 login attempts per minute per IP
- **Implementation**: RateLimitingFilter
- **Scope**: `/auth/login` endpoint

### Failed Login Tracking

Database tracks failed login attempts:
```sql
intentos_fallidos_login INTEGER DEFAULT 0,
bloqueado_hasta TIMESTAMP
```

**Future Enhancement**: Implement account lockout after N failed attempts

## Database Security

### Connection Security

- **SSL/TLS**: Enabled for PostgreSQL connections in production
- **Connection Pool**: HikariCP with leak detection
- **Credentials**: Stored in environment variables (not in code)

### SQL Injection Prevention

- **JPA/Hibernate**: Parameterized queries by default
- **Repository Pattern**: Type-safe query methods
- **Input Validation**: Bean Validation (JSR-380)

### Database Access

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}  # Never hardcode credentials
```

## API Security

### Endpoint Protection

```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers("/auth/**").permitAll()
    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
    .requestMatchers("/actuator/info").permitAll()
    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
    
    // Protected endpoints
    .requestMatchers("/usuarios/**").authenticated()
    .anyRequest().authenticated()
)
```

### Actuator Security

Health check endpoints are public for Render monitoring:
- `/actuator/health` - Public (required for health checks)
- `/actuator/health/liveness` - Public
- `/actuator/health/readiness` - Public
- `/actuator/info` - Public
- `/actuator/metrics` - Requires authentication (future)

**Production Recommendation**: Restrict actuator endpoints to internal network only.

## Input Validation

### Bean Validation

```java
public class LoginRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    private String password;
}
```

### Validation Groups

Use validation groups for different scenarios:
- Create operations
- Update operations
- Partial updates

## Error Handling

### Security Error Responses

Never expose sensitive information in error messages:

```java
@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ErrorResponse> handleAuthenticationException(
    AuthenticationException ex) {
    // Generic message - don't reveal if user exists
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Invalid credentials"));
}
```

### Logging Security Events

```java
logger.warn("Failed login attempt for user: {}", username);
logger.error("Invalid JWT signature: {}", ex.getMessage());
```

**Important**: Never log sensitive data (passwords, tokens, PII)

## Dependency Security

### Vulnerability Scanning

Run security scans regularly:

```bash
# Maven dependency check
./mvnw org.owasp:dependency-check-maven:check

# Update dependencies
./mvnw versions:display-dependency-updates
```

### Keeping Dependencies Updated

- Spring Boot: Update to latest patch version
- Spring Security: Critical security updates
- JWT library: Monitor for vulnerabilities
- PostgreSQL driver: Keep updated

## Environment Variables

### Required Variables

```bash
# Production
DATABASE_URL=postgresql://user:pass@host:5432/db
JWT_SECRET=<minimum-64-characters-random-string>
SPRING_PROFILES_ACTIVE=prod

# Optional
JAVA_OPTS=-Xmx512m -Xms256m
```

### Generating Secure Secrets

```bash
# JWT Secret (64+ characters)
openssl rand -base64 64

# Alternative
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

### Secret Management

- ✅ Use Render environment variables
- ✅ Never commit secrets to Git
- ✅ Rotate secrets periodically
- ✅ Use different secrets per environment

## Monitoring and Auditing

### Security Logging

Log security-relevant events:
- Authentication attempts (success/failure)
- Authorization failures
- JWT validation errors
- Rate limit violations
- Suspicious activity

### Audit Trail

Database audit fields track changes:
```sql
creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
creado_por VARCHAR(50),
actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
actualizado_por VARCHAR(50)
```

## Security Checklist

### Pre-Deployment

- [ ] JWT_SECRET is strong (64+ characters) and unique
- [ ] DATABASE_URL uses internal connection
- [ ] CORS configured for production domain only
- [ ] Security headers enabled
- [ ] HTTPS enforcement configured
- [ ] Rate limiting enabled
- [ ] Input validation implemented
- [ ] Error messages don't expose sensitive info
- [ ] Dependencies are up to date
- [ ] No secrets in code or Git history

### Post-Deployment

- [ ] Verify HTTPS is working
- [ ] Test CORS from frontend domain
- [ ] Verify security headers in browser
- [ ] Test authentication flow
- [ ] Check health endpoints are accessible
- [ ] Monitor logs for security events
- [ ] Test rate limiting
- [ ] Verify JWT expiration works

## Incident Response

### If JWT Secret is Compromised

1. Generate new JWT_SECRET
2. Update environment variable in Render
3. Restart application
4. All existing tokens become invalid
5. Users must re-authenticate

### If Database Credentials are Compromised

1. Rotate database password in Render
2. Update DATABASE_URL environment variable
3. Restart application
4. Review database logs for suspicious activity

### If Vulnerability is Discovered

1. Assess severity and impact
2. Update affected dependency
3. Test thoroughly
4. Deploy fix immediately
5. Document incident and response

## Security Best Practices

### Development

- Use HTTPS even in development (optional)
- Never commit secrets to Git
- Use `.env` files for local development (gitignored)
- Keep dependencies updated
- Run security scans regularly

### Production

- Monitor security logs daily
- Rotate secrets quarterly
- Update dependencies monthly
- Review access logs weekly
- Perform security audits annually

## Compliance Considerations

### Data Protection

- Passwords are hashed (BCrypt)
- Sensitive data encrypted in transit (HTTPS)
- Database connections encrypted (SSL/TLS)
- Audit trail for data changes

### GDPR Considerations

- User data minimization
- Right to be forgotten (implement user deletion)
- Data portability (implement data export)
- Consent management (future enhancement)

## Future Security Enhancements

### Short Term

1. Implement account lockout after failed attempts
2. Add email verification for new accounts
3. Implement password reset functionality
4. Add two-factor authentication (2FA)
5. Move JWT to HTTP-only cookies

### Long Term

1. Implement OAuth2/OpenID Connect
2. Add API rate limiting per user
3. Implement IP whitelisting for admin endpoints
4. Add security event notifications
5. Implement automated security scanning in CI/CD
6. Add Web Application Firewall (WAF)
7. Implement intrusion detection system

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Render Security](https://render.com/docs/security)
