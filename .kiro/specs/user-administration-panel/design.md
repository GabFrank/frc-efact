# Design Document

## Overview

The User Administration Panel will be implemented as a comprehensive Angular feature module following the established patterns in the FRC eFact application. The design leverages Angular standalone components, NgRx state management, Material Design components, and follows the same architectural patterns used in the empresas feature.

The panel will provide full CRUD operations for user management, role assignment, password management, and account status control, all secured behind administrator-only access controls.

## Architecture

### Component Structure
```
src/app/features/usuarios/
├── usuarios-list.component.ts          # Main user list with search and actions
├── usuario-form.component.ts           # Create/Edit user form
├── usuario-detail.component.ts         # View user details
├── reset-password-dialog.component.ts  # Password reset dialog
├── usuarios.routes.ts                  # Feature routing
└── index.ts                           # Feature exports
```

### State Management Structure
```
src/app/core/state/usuarios/
├── usuarios.actions.ts     # NgRx actions for user operations
├── usuarios.effects.ts     # Side effects for API calls
├── usuarios.reducer.ts     # State reducer
└── usuarios.selectors.ts   # State selectors
```

### API Service Structure
```
src/app/core/api/
└── usuario-api.service.ts  # HTTP service for user API calls
```

### Models Enhancement
```
src/app/models/
├── user.model.ts          # Enhanced user models
└── role.model.ts          # Role-related models
```

## Components and Interfaces

### 1. UsuariosListComponent

**Purpose:** Main component displaying paginated user list with search and management actions.

**Key Features:**
- Paginated table with sorting capabilities
- Real-time search by username/email with debouncing
- Status indicators (active/inactive/locked)
- Action menu for each user (view, edit, reset password, toggle status, delete)
- Bulk operations support
- Role-based action visibility

**Template Structure:**
```html
<div class="usuarios-container">
  <mat-card>
    <mat-card-header>
      <mat-card-title>Gestión de Usuarios</mat-card-title>
    </mat-card-header>
    <mat-card-content>
      <!-- Search and Actions Bar -->
      <div class="actions-bar">
        <mat-form-field class="search-field">
          <input matInput [(ngModel)]="searchTerm" placeholder="Buscar usuarios...">
        </mat-form-field>
        <button mat-raised-button color="primary" (click)="onCreateUser()">
          <mat-icon>add</mat-icon>
          Nuevo Usuario
        </button>
      </div>

      <!-- Users Table -->
      <table mat-table [dataSource]="filteredUsers" class="usuarios-table">
        <!-- Columns: username, email, roles, status, lastLogin, actions -->
      </table>
    </mat-card-content>
  </mat-card>
</div>
```

### 2. UsuarioFormComponent

**Purpose:** Form component for creating and editing users with comprehensive validation.

**Key Features:**
- Reactive forms with comprehensive validation
- Role assignment with multi-select capability
- Password complexity validation for new users
- Real-time username/email uniqueness validation
- Company assignment interface
- Form mode detection (create/edit/view)

**Form Structure:**
```typescript
createForm(): FormGroup {
  return this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', this.isEditMode ? [] : [Validators.required, Validators.minLength(8)]],
    isActive: [true],
    roles: [[], Validators.required],
    empresas: [[]]
  });
}
```

### 3. UsuarioDetailComponent

**Purpose:** Read-only detailed view of user information with quick action buttons.

**Key Features:**
- Complete user information display
- Company assignments with roles
- Login history and account status
- Quick action buttons (edit, reset password, toggle status)
- Audit trail information

### 4. ResetPasswordDialogComponent

**Purpose:** Modal dialog for administrator password reset functionality.

**Key Features:**
- Secure password generation option
- Password complexity validation
- Force password change on next login option
- Confirmation workflow

## Data Models

### Enhanced User Model
```typescript
export interface User {
  id: number;
  username: string;
  email: string;
  isActive: boolean;
  roles: string[];
  lastLogin?: string;
  createdAt: string;
  updatedAt: string;
  failedLoginAttempts: number;
  lockedUntil?: string;
  empresas?: UsuarioEmpresa[];
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  roles: string[];
  isActive: boolean;
}

export interface UpdateUserRequest {
  username?: string;
  email?: string;
  roles?: string[];
  isActive?: boolean;
}

export interface ResetPasswordRequest {
  userId: number;
  newPassword: string;
  forcePasswordChange?: boolean;
}
```

### Role Model
```typescript
export interface Role {
  id: number;
  name: string;
  description: string;
  permissions: string[];
}

export interface UserRole {
  userId: number;
  roleId: number;
  assignedAt: string;
  assignedBy: number;
}
```

## State Management Design

### Actions
```typescript
// Load actions
export const loadUsers = createAction('[Users] Load Users');
export const loadUsersSuccess = createAction('[Users] Load Users Success', props<{ users: User[] }>());
export const loadUsersFailure = createAction('[Users] Load Users Failure', props<{ error: string }>());

// CRUD actions
export const createUser = createAction('[Users] Create User', props<{ user: CreateUserRequest }>());
export const updateUser = createAction('[Users] Update User', props<{ id: number; user: UpdateUserRequest }>());
export const deleteUser = createAction('[Users] Delete User', props<{ id: number }>());

// Password management
export const resetPassword = createAction('[Users] Reset Password', props<{ request: ResetPasswordRequest }>());

// Account management
export const toggleUserStatus = createAction('[Users] Toggle User Status', props<{ id: number }>());
export const unlockUser = createAction('[Users] Unlock User', props<{ id: number }>());
```

### State Structure
```typescript
export interface UsersState {
  users: User[];
  selectedUser: User | null;
  loading: boolean;
  error: string | null;
  searchTerm: string;
  pagination: {
    page: number;
    size: number;
    total: number;
  };
}
```

### Selectors
```typescript
export const selectAllUsers = createSelector(selectUsersState, state => state.users);
export const selectUsersLoading = createSelector(selectUsersState, state => state.loading);
export const selectUsersError = createSelector(selectUsersState, state => state.error);
export const selectUserById = (id: number) => createSelector(selectAllUsers, users => users.find(u => u.id === id));
export const selectActiveUsers = createSelector(selectAllUsers, users => users.filter(u => u.isActive));
export const selectLockedUsers = createSelector(selectAllUsers, users => users.filter(u => u.lockedUntil && new Date(u.lockedUntil) > new Date()));
```

## API Service Design

### UsuarioApiService
```typescript
@Injectable({ providedIn: 'root' })
export class UsuarioApiService {
  private readonly baseUrl = `${environment.apiUrl}/usuarios`;

  // CRUD operations
  getAll(): Observable<User[]>
  getById(id: number): Observable<User>
  create(user: CreateUserRequest): Observable<User>
  update(id: number, user: UpdateUserRequest): Observable<User>
  delete(id: number): Observable<void>

  // Password management
  resetPassword(request: ResetPasswordRequest): Observable<void>
  changePassword(userId: number, oldPassword: string, newPassword: string): Observable<void>

  // Account management
  activateUser(id: number): Observable<User>
  deactivateUser(id: number): Observable<User>
  unlockUser(id: number): Observable<User>

  // Role management
  assignRole(userId: number, roleId: number): Observable<void>
  removeRole(userId: number, roleId: number): Observable<void>
  getUserRoles(userId: number): Observable<Role[]>

  // Search and filtering
  searchUsers(term: string): Observable<User[]>
  getUsersByRole(role: string): Observable<User[]>
}
```

## Routing and Navigation

### Route Configuration
```typescript
export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    component: UsuariosListComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'new',
    component: UsuarioFormComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: ':id',
    component: UsuarioDetailComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: ':id/edit',
    component: UsuarioFormComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  }
];
```

### Navigation Integration
The main layout component will be updated to include the "Usuarios" menu item, visible only to administrators:

```typescript
// In main-layout.component.ts
get showUsuariosMenu(): boolean {
  return this.userRole === 'ADMIN';
}
```

## Error Handling

### Validation Strategy
- **Frontend Validation:** Real-time form validation with immediate feedback
- **Backend Validation:** Server-side validation with detailed error messages
- **Unique Constraint Handling:** Async validators for username/email uniqueness
- **Password Policy:** Configurable password complexity requirements

### Error Display
- **Form Errors:** Field-level error messages with Material Design styling
- **API Errors:** Toast notifications for operation results
- **Network Errors:** Retry mechanisms with user feedback
- **Permission Errors:** Clear access denied messages with navigation guidance

## Security Considerations

### Access Control
- **Route Guards:** Role-based guards preventing unauthorized access
- **Component-Level Security:** Hide/disable actions based on user permissions
- **API Security:** JWT token validation on all endpoints
- **Audit Logging:** Track all administrative actions

### Data Protection
- **Password Handling:** Never display or log actual passwords
- **Sensitive Data:** Mask or exclude sensitive information in logs
- **Session Management:** Automatic logout on privilege escalation
- **CSRF Protection:** Anti-forgery tokens for state-changing operations

## Testing Strategy

### Unit Testing
- **Component Testing:** Test all user interactions and state changes
- **Service Testing:** Mock HTTP calls and test error handling
- **State Testing:** Verify NgRx actions, reducers, and selectors
- **Validation Testing:** Test all form validation scenarios

### Integration Testing
- **API Integration:** Test actual API calls with test data
- **Navigation Testing:** Verify routing and guard behavior
- **Permission Testing:** Test role-based access controls
- **End-to-End Testing:** Complete user management workflows

### Test Coverage Goals
- **Components:** 90% code coverage
- **Services:** 95% code coverage
- **State Management:** 100% coverage for critical paths
- **Guards and Interceptors:** 100% coverage

## Performance Considerations

### Optimization Strategies
- **Lazy Loading:** Feature module loaded on demand
- **Virtual Scrolling:** For large user lists
- **Debounced Search:** Prevent excessive API calls
- **Caching:** Cache user data with appropriate TTL
- **Pagination:** Server-side pagination for scalability

### Memory Management
- **Subscription Management:** Proper cleanup with takeUntil pattern
- **State Cleanup:** Clear user data on navigation
- **Component Lifecycle:** Efficient OnDestroy implementations
- **Change Detection:** OnPush strategy where appropriate

## Accessibility

### WCAG Compliance
- **Keyboard Navigation:** Full keyboard accessibility
- **Screen Reader Support:** Proper ARIA labels and descriptions
- **Color Contrast:** Meet WCAG AA standards
- **Focus Management:** Logical tab order and focus indicators

### Internationalization
- **Text Externalization:** All user-facing text in translation files
- **Date/Time Formatting:** Locale-aware formatting
- **Number Formatting:** Regional number formats
- **RTL Support:** Right-to-left language support preparation