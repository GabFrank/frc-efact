# FRC eFact Frontend - Security Configuration

## Overview

This document describes the security measures implemented in the FRC eFact frontend application.

## HTTPS Configuration

### HTTPS Interceptor

The application includes an HTTPS interceptor that ensures all API requests use HTTPS in production:

```typescript
export const httpsInterceptor: HttpInterceptorFn = (req, next) => {
  if (environment.production && environment.enableHttps) {
    if (req.url.startsWith('http://')) {
      const httpsReq = req.clone({
        url: req.url.replace('http://', 'https://')
      });
      return next(httpsReq);
    }
  }
  return next(req);
};
```

### Environment Configuration

**Production**:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://frc-efact-backend.onrender.com/api',
  enableHttps: true,
  secureOnly: true
};
```

**Development**:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  enableHttps: false,
  secureOnly: false
};
```

## Security Headers

### Render Configuration

Security headers are configured in `render.yaml`:

```yaml
headers:
  - path: /*
    name: X-Frame-Options
    value: DENY
  - path: /*
    name: X-Content-Type-Options
    value: nosniff
  - path: /*
    name: X-XSS-Protection
    value: 1; mode=block
  - path: /*
    name: Referrer-Policy
    value: strict-origin-when-cross-origin
  - path: /*
    name: Permissions-Policy
    value: geolocation=(), microphone=(), camera=()
```

### Header Descriptions

| Header | Value | Purpose |
|--------|-------|---------|
| X-Frame-Options | DENY | Prevents clickjacking by disallowing iframe embedding |
| X-Content-Type-Options | nosniff | Prevents MIME type sniffing |
| X-XSS-Protection | 1; mode=block | Enables browser XSS filter |
| Referrer-Policy | strict-origin-when-cross-origin | Controls referrer information |
| Permissions-Policy | geolocation=(), microphone=(), camera=() | Restricts browser features |

### Content Security Policy (Future)

Consider adding CSP headers for enhanced security:

```yaml
- path: /*
  name: Content-Security-Policy
  value: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https://frc-efact-backend.onrender.com
```

**Note**: Angular Material requires `unsafe-inline` for styles. Consider using nonces in future versions.

## Authentication Security

### JWT Token Storage

**Current Implementation**: In-memory storage

```typescript
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private tokenSubject = new BehaviorSubject<string | null>(null);
  
  // Token stored in memory, not localStorage
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.tokenSubject.next(response.token);
          this.currentUserSubject.next(response.user);
        })
      );
  }
}
```

**Advantages**:
- ✅ Not vulnerable to XSS attacks on localStorage
- ✅ Token cleared on browser close
- ✅ No persistence across sessions

**Disadvantages**:
- ⚠️ Token lost on page refresh (requires re-login)
- ⚠️ Not suitable for long-lived sessions

### Alternative: HTTP-Only Cookies (Future Enhancement)

```typescript
// Backend sends token as HTTP-only cookie
// Frontend doesn't need to handle token storage
login(credentials: LoginRequest): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(
    `${this.apiUrl}/auth/login`, 
    credentials,
    { withCredentials: true }  // Include cookies
  );
}
```

### Auth Interceptor

Automatically adds JWT token to requests:

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  return next(req);
};
```

## Route Protection

### Auth Guard

Protects routes from unauthorized access:

```typescript
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Redirect to login with return URL
  router.navigate(['/login'], { 
    queryParams: { returnUrl: state.url } 
  });
  return false;
};
```

### No-Auth Guard

Prevents authenticated users from accessing login page:

```typescript
export const noAuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  // Redirect to welcome page if already authenticated
  router.navigate(['/welcome']);
  return false;
};
```

### Route Configuration

```typescript
export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [noAuthGuard]
  },
  {
    path: 'welcome',
    component: WelcomeComponent,
    canActivate: [authGuard]
  }
];
```

## Input Validation

### Form Validation

Use Angular Reactive Forms with validators:

```typescript
loginForm = this.fb.group({
  username: ['', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(50)
  ]],
  password: ['', [
    Validators.required,
    Validators.minLength(8),
    Validators.maxLength(100)
  ]]
});
```

### Custom Validators

```typescript
export class CustomValidators {
  static strongPassword(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumeric = /[0-9]/.test(value);
    const hasSpecial = /[!@#$%^&*]/.test(value);

    const valid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecial;
    return valid ? null : { strongPassword: true };
  }
}
```

### Sanitization

Angular automatically sanitizes values to prevent XSS:

```typescript
// Safe - Angular sanitizes by default
<div>{{ userInput }}</div>

// Unsafe - bypasses sanitization (avoid)
<div [innerHTML]="userInput"></div>

// Safe with sanitization
import { DomSanitizer } from '@angular/platform-browser';

constructor(private sanitizer: DomSanitizer) {}

getSafeHtml(html: string) {
  return this.sanitizer.sanitize(SecurityContext.HTML, html);
}
```

## Error Handling

### Error Interceptor

Handles HTTP errors globally:

```typescript
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Unauthorized - redirect to login
        authService.logout();
        router.navigate(['/login']);
      } else if (error.status === 403) {
        // Forbidden - show error message
        console.error('Access denied');
      }
      return throwError(() => error);
    })
  );
};
```

### Never Expose Sensitive Information

```typescript
// Bad - exposes internal error
catchError(error => {
  alert(error.message);  // May contain sensitive info
});

// Good - generic user message
catchError(error => {
  console.error('Error:', error);  // Log for debugging
  this.showError('An error occurred. Please try again.');  // User message
});
```

## Dependency Security

### Regular Updates

Keep dependencies updated:

```bash
# Check for outdated packages
npm outdated

# Update dependencies
npm update

# Audit for vulnerabilities
npm audit

# Fix vulnerabilities
npm audit fix
```

### Critical Dependencies

Monitor these for security updates:
- `@angular/core` - Core framework
- `@angular/common` - Common utilities
- `@angular/router` - Routing
- `rxjs` - Reactive programming

### Vulnerability Scanning

Run security audits regularly:

```bash
# NPM audit
npm audit

# Snyk (install globally)
npm install -g snyk
snyk test
```

## Build Security

### Production Build

The production build includes security optimizations:

```json
{
  "optimization": {
    "scripts": true,
    "styles": {
      "minify": true,
      "inlineCritical": true
    }
  },
  "sourceMap": false,
  "extractLicenses": true
}
```

**Security Benefits**:
- Source maps disabled (prevents code inspection)
- Code minification (harder to reverse engineer)
- Tree shaking (removes unused code)
- License extraction (compliance)

### Environment Variables

Never commit sensitive data:

```typescript
// Bad - hardcoded API key
const API_KEY = 'sk_live_abc123';

// Good - use environment variables
const API_KEY = environment.apiKey;
```

### .gitignore

Ensure sensitive files are ignored:

```
# Environment files
.env
.env.local
.env.*.local

# Build output
/dist
/tmp

# Dependencies
/node_modules

# IDE
.vscode/
.idea/
```

## CORS Security

### Backend Configuration

CORS is configured on the backend to allow only specific origins:

```yaml
cors:
  allowed-origins:
    - https://frc-efact-frontend.onrender.com
```

### Frontend Requests

Include credentials for CORS requests:

```typescript
this.http.post(url, data, {
  withCredentials: true  // Include cookies
});
```

## XSS Prevention

### Angular's Built-in Protection

Angular provides automatic XSS protection:

1. **Template Interpolation**: `{{ value }}` - Automatically escaped
2. **Property Binding**: `[property]="value"` - Sanitized
3. **Attribute Binding**: `[attr.href]="value"` - Sanitized

### Dangerous Patterns to Avoid

```typescript
// Dangerous - bypasses sanitization
<div [innerHTML]="userInput"></div>

// Dangerous - direct DOM manipulation
this.elementRef.nativeElement.innerHTML = userInput;

// Dangerous - eval
eval(userInput);

// Dangerous - Function constructor
new Function(userInput)();
```

### Safe Alternatives

```typescript
// Safe - use text content
<div [textContent]="userInput"></div>

// Safe - use renderer
this.renderer.setProperty(element, 'textContent', userInput);

// Safe - sanitize if HTML is needed
import { DomSanitizer } from '@angular/platform-browser';

constructor(private sanitizer: DomSanitizer) {}

getSafeHtml(html: string) {
  return this.sanitizer.sanitize(SecurityContext.HTML, html);
}
```

## CSRF Protection

### Token-Based Authentication

JWT tokens provide CSRF protection:
- Tokens stored in memory (not cookies)
- Sent via Authorization header
- Not automatically included in requests

### Future: Cookie-Based Authentication

If using cookies, implement CSRF protection:

```typescript
// Backend sends CSRF token
// Frontend includes token in requests
this.http.post(url, data, {
  headers: {
    'X-CSRF-Token': this.getCsrfToken()
  }
});
```

## Security Checklist

### Development

- [ ] Use HTTPS in development (optional)
- [ ] Never commit secrets to Git
- [ ] Use environment variables for configuration
- [ ] Keep dependencies updated
- [ ] Run security audits regularly
- [ ] Use Angular's built-in security features
- [ ] Validate all user input
- [ ] Sanitize dynamic content

### Pre-Deployment

- [ ] Production build enabled
- [ ] Source maps disabled
- [ ] API URL points to production backend
- [ ] HTTPS enforced
- [ ] Security headers configured
- [ ] CORS configured correctly
- [ ] No console.log statements with sensitive data
- [ ] Error messages don't expose sensitive info
- [ ] Dependencies are up to date
- [ ] No known vulnerabilities (npm audit)

### Post-Deployment

- [ ] Verify HTTPS is working
- [ ] Test authentication flow
- [ ] Verify route guards work
- [ ] Check security headers in browser
- [ ] Test CORS from production domain
- [ ] Verify error handling
- [ ] Monitor browser console for errors
- [ ] Test on multiple browsers

## Monitoring and Logging

### Client-Side Logging

Implement structured logging:

```typescript
export class LoggerService {
  log(message: string, data?: any) {
    if (!environment.production) {
      console.log(message, data);
    }
  }

  error(message: string, error?: any) {
    console.error(message, error);
    // Send to error tracking service in production
    if (environment.production) {
      this.sendToErrorTracking(message, error);
    }
  }
}
```

### Error Tracking

Consider integrating error tracking:
- Sentry
- Rollbar
- LogRocket
- Bugsnag

### Security Event Logging

Log security-relevant events:
- Login attempts
- Logout events
- Authorization failures
- Token expiration
- API errors

## Best Practices

### General

1. **Principle of Least Privilege**: Only request permissions needed
2. **Defense in Depth**: Multiple layers of security
3. **Fail Securely**: Default to deny access
4. **Keep it Simple**: Complex code is harder to secure
5. **Security by Design**: Consider security from the start

### Angular-Specific

1. Use Angular's built-in security features
2. Keep Angular updated to latest version
3. Use TypeScript strict mode
4. Avoid `any` type (use proper types)
5. Use reactive forms with validation
6. Implement route guards
7. Use interceptors for cross-cutting concerns
8. Sanitize dynamic content
9. Avoid direct DOM manipulation
10. Use Angular CLI for builds

## Incident Response

### If Token is Compromised

1. User logs out immediately
2. Token expires automatically (24 hours)
3. User must re-authenticate
4. Monitor for suspicious activity

### If Vulnerability is Discovered

1. Assess severity and impact
2. Update affected dependency
3. Test thoroughly
4. Deploy fix immediately
5. Notify users if necessary
6. Document incident and response

## Future Enhancements

### Short Term

1. Implement refresh token rotation
2. Add session timeout warning
3. Implement "remember me" functionality
4. Add password strength meter
5. Implement email verification

### Long Term

1. Add two-factor authentication (2FA)
2. Implement biometric authentication
3. Add OAuth2/Social login
4. Implement Content Security Policy
5. Add Subresource Integrity (SRI)
6. Implement rate limiting on client
7. Add Web Application Firewall (WAF)

## Resources

- [Angular Security Guide](https://angular.io/guide/security)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/)
- [MDN Web Security](https://developer.mozilla.org/en-US/docs/Web/Security)
- [Render Security](https://render.com/docs/security)
