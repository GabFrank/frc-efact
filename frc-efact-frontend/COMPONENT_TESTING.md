# Component Testing Guide

## Components Implemented

### 1. Login Component (`/login`)
**Location:** `src/app/components/login/`

**Features:**
- Reactive form with validation
- Username field (min 3 characters)
- Password field (min 6 characters) with show/hide toggle
- Real-time validation error messages
- Loading spinner during authentication
- Error handling for different HTTP status codes (401, 429, 0)
- Material Design UI with responsive layout
- Data attributes for E2E testing (`data-cy`)

**Validation Rules:**
- Username: Required, minimum 3 characters
- Password: Required, minimum 6 characters

**Error Messages:**
- 401: "Usuario o contraseña incorrectos"
- 429: "Demasiados intentos. Por favor, intente más tarde"
- 0: "No se pudo conectar con el servidor"
- Default: "Error al iniciar sesión. Por favor, intente nuevamente"

### 2. Welcome Component (`/welcome`)
**Location:** `src/app/components/welcome/`

**Features:**
- Material Design toolbar with branding
- User menu with profile information
- Logout functionality
- Dynamic greeting based on time of day
- Dashboard grid with 4 placeholder cards:
  - User Profile (shows username, email, ID)
  - Documents (placeholder for future functionality)
  - Reports (placeholder for future functionality)
  - Settings (placeholder for future functionality)
- Fully responsive design
- Smooth hover animations on cards

### 3. App Component (Main Layout)
**Location:** `src/app/`

**Features:**
- Clean router outlet container
- Minimal styling for proper layout

## Routing Configuration

### Routes:
- `/` → Redirects to `/login`
- `/login` → Login component (protected by `noAuthGuard`)
- `/welcome` → Welcome component (protected by `authGuard`)
- `/**` → Redirects to `/login`

### Guards:
- **authGuard**: Protects routes that require authentication
  - Redirects to `/login` if not authenticated
  - Preserves return URL in query params
  
- **noAuthGuard**: Prevents authenticated users from accessing login
  - Redirects to `/welcome` if already authenticated

## Testing the Application

### Prerequisites:
1. Backend must be running on `http://localhost:8080`
2. Database must be initialized with at least one user

### Manual Testing Steps:

#### 1. Test Login Flow:
```bash
# Start the frontend
cd frc-efact-frontend
npm start
```

1. Navigate to `http://localhost:4200`
2. Should redirect to `/login`
3. Try submitting empty form → See validation errors
4. Enter invalid credentials → See error message
5. Enter valid credentials → Redirect to `/welcome`

#### 2. Test Welcome Page:
1. After successful login, verify:
   - Toolbar shows "FRC eFact" branding
   - User menu shows username and email
   - Dashboard shows 4 cards
   - Greeting changes based on time of day
2. Click user menu → Click "Cerrar Sesión"
3. Should redirect to `/login`

#### 3. Test Guards:
1. While logged out, try to access `/welcome` directly
   - Should redirect to `/login`
2. While logged in, try to access `/login` directly
   - Should redirect to `/welcome`

#### 4. Test Responsive Design:
1. Resize browser window
2. Verify layout adapts properly:
   - Mobile: Single column, simplified toolbar
   - Tablet: Adjusted grid
   - Desktop: Full layout

### Test Credentials:
Use the credentials from your database. If you ran the migration V3, you should have:
- Username: `admin`
- Password: `admin123` (or as configured in your migration)

## Component Architecture

### Login Component:
```
LoginComponent
├── Reactive Form (FormBuilder)
├── AuthService (login method)
├── Router (navigation)
└── Material Design modules
```

### Welcome Component:
```
WelcomeComponent
├── AuthService (getCurrentUser, logout)
├── Router (navigation)
├── Material Design modules
└── User data display
```

## Next Steps

To extend the application:

1. **Add more routes** in `app.routes.ts`
2. **Create new components** for Documents, Reports, Settings
3. **Implement lazy loading** for feature modules
4. **Add more guards** for role-based access
5. **Enhance error handling** with toast notifications
6. **Add loading states** for better UX
7. **Implement form validation** for additional forms

## Troubleshooting

### Issue: Components not loading
- Check that all imports are correct
- Verify Angular Material is installed
- Check browser console for errors

### Issue: Login fails
- Verify backend is running
- Check API URL in `environment.ts`
- Verify CORS is configured in backend
- Check network tab for request/response

### Issue: Routing not working
- Verify guards are properly configured
- Check that AuthService.isAuthenticated() works
- Verify tokens are stored in localStorage

### Issue: Styling issues
- Verify Angular Material theme is imported
- Check that global styles are loaded
- Verify component styles are applied
