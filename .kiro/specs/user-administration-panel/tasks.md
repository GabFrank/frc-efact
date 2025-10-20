# Implementation Plan

- [x] 1. Set up backend API endpoints for user administration
  - Create comprehensive UsuarioController with admin endpoints
  - Implement user CRUD operations with proper validation
  - Add password reset and account management endpoints
  - Add role management endpoints
  - Implement search and filtering capabilities
  - Add proper security annotations and role-based access control
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1, 9.1, 10.1_

- [x] 2. Create enhanced user models and DTOs
  - [x] 2.1 Update User model interface with additional fields
    - Add failedLoginAttempts, lockedUntil, and other admin fields
    - Include role information and company assignments
    - Add audit fields for creation and modification tracking
    - _Requirements: 2.2, 7.1, 8.1, 9.2_

  - [x] 2.2 Create user management request/response DTOs
    - Implement CreateUserRequest interface
    - Implement UpdateUserRequest interface  
    - Implement ResetPasswordRequest interface
    - Create Role and UserRole interfaces
    - _Requirements: 3.2, 4.2, 5.2, 8.2_

- [x] 3. Implement user API service
  - [x] 3.1 Create UsuarioApiService with comprehensive methods
    - Implement CRUD operations (getAll, getById, create, update, delete)
    - Add password management methods (resetPassword, changePassword)
    - Add account management methods (activate, deactivate, unlock)
    - Add role management methods (assignRole, removeRole, getUserRoles)
    - Add search and filtering methods
    - _Requirements: 1.3, 2.1, 3.3, 4.3, 5.3, 6.3, 7.3, 8.3, 9.3, 10.3_

- [x] 4. Set up NgRx state management for users
  - [x] 4.1 Create user actions
    - Implement load actions (loadUsers, loadUsersSuccess, loadUsersFailure)
    - Create CRUD actions (createUser, updateUser, deleteUser with success/failure variants)
    - Add password management actions (resetPassword)
    - Add account management actions (toggleUserStatus, unlockUser)
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 10.1_

  - [x] 4.2 Implement user reducer
    - Define UsersState interface with users array, loading, error, and pagination
    - Handle all user actions with proper state updates
    - Implement proper error handling and loading states
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 10.1_

  - [x] 4.3 Create user selectors
    - Implement selectAllUsers, selectUsersLoading, selectUsersError
    - Create selectUserById selector factory
    - Add filtered selectors (selectActiveUsers, selectLockedUsers)
    - _Requirements: 2.1, 7.1, 9.1_

  - [x] 4.4 Implement user effects
    - Handle API calls for all user operations
    - Implement proper error handling with user-friendly messages
    - Add success notifications for operations
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 10.1_

- [x] 5. Create user list component
  - [x] 5.1 Implement UsuariosListComponent structure
    - Create component with Material Design table
    - Add search functionality with debounced input
    - Implement pagination and sorting
    - Add loading and error states
    - _Requirements: 1.1, 2.1, 2.6, 2.7_

  - [x] 5.2 Add user status indicators and action menu
    - Implement color-coded status chips (active/inactive/locked)
    - Create action menu with view, edit, reset password, toggle status, delete options
    - Add confirmation dialogs for destructive actions
    - Implement role-based action visibility
    - _Requirements: 2.3, 2.4, 6.1, 7.1, 10.1_

  - [x] 5.3 Implement search and filtering functionality
    - Add real-time search with debouncing
    - Filter by username and email
    - Display "no users found" state
    - _Requirements: 2.5, 2.6, 2.7_

- [x] 6. Create user form component
  - [x] 6.1 Implement UsuarioFormComponent with reactive forms
    - Create comprehensive form with validation
    - Add username, email, password fields with proper validation
    - Implement role assignment with multi-select
    - Add form mode detection (create/edit/view)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6, 4.1, 4.2, 4.3_

  - [x] 6.2 Add form validation and error handling
    - Implement async validators for username/email uniqueness
    - Add password complexity validation
    - Display field-level error messages
    - Handle form submission with proper error feedback
    - _Requirements: 3.3, 3.4, 3.5, 3.8, 4.4, 4.6_

  - [x] 6.3 Implement form submission and navigation
    - Handle create and update operations
    - Show success messages and redirect to user list
    - Implement cancel functionality
    - _Requirements: 3.7, 3.9, 4.5, 4.7, 4.8_

- [x] 7. Create user detail component
  - [x] 7.1 Implement UsuarioDetailComponent
    - Display comprehensive user information
    - Show company assignments and roles
    - Display account status and login history
    - Add quick action buttons (edit, reset password, toggle status)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 8. Create password reset dialog component
  - [x] 8.1 Implement ResetPasswordDialogComponent
    - Create modal dialog with password input
    - Add password complexity validation
    - Implement secure password generation option
    - Add force password change checkbox
    - Handle confirmation workflow
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [x] 9. Set up routing and navigation
  - [x] 9.1 Create usuarios routing module
    - Define routes for list, create, edit, and detail views
    - Add role-based guards for admin-only access
    - Implement proper route parameters handling
    - _Requirements: 1.1, 1.2, 3.1, 4.1, 9.1_

  - [x] 9.2 Update main layout navigation
    - Add "Usuarios" menu item in sidebar
    - Show menu item only for admin users
    - Update navigation styling and icons
    - _Requirements: 1.4, 1.5_

- [x] 10. Implement security and access control
  - [x] 10.1 Update role guard for user administration
    - Ensure only ADMIN users can access user management
    - Add proper error handling for unauthorized access
    - Implement redirect logic for non-admin users
    - _Requirements: 1.2, 1.4_

  - [x] 10.2 Add component-level security
    - Hide/disable actions based on user permissions
    - Prevent self-deletion for current admin user
    - Add audit logging for administrative actions
    - _Requirements: 10.5_

- [x] 11. Add comprehensive error handling and user feedback
  - [x] 11.1 Implement form validation and error display
    - Add real-time validation feedback
    - Display server-side validation errors
    - Handle unique constraint violations gracefully
    - _Requirements: 3.8, 4.6_

  - [x] 11.2 Add operation feedback and notifications
    - Show success messages for all operations
    - Display error notifications with retry options
    - Add loading indicators for long operations
    - _Requirements: 3.7, 4.5, 5.4, 6.3, 7.4, 10.4_

- [ ] 12. Implement advanced features
  - [ ] 12.1 Add bulk operations support
    - Implement multi-select functionality in user list
    - Add bulk activate/deactivate operations
    - Add bulk role assignment capabilities
    - _Requirements: 6.1, 6.2, 8.1, 8.2_

  - [ ] 12.2 Add user activity monitoring
    - Display login history in user details
    - Show failed login attempts and lock status
    - Add user activity timeline
    - _Requirements: 7.1, 7.2, 9.2, 9.3_

- [ ]* 13. Add comprehensive testing
  - [ ]* 13.1 Write unit tests for components
    - Test UsuariosListComponent interactions and state changes
    - Test UsuarioFormComponent validation and submission
    - Test ResetPasswordDialogComponent workflow
    - Test UsuarioDetailComponent display and actions

  - [ ]* 13.2 Write unit tests for services and state management
    - Test UsuarioApiService HTTP calls and error handling
    - Test NgRx actions, reducers, and selectors
    - Test effects and side effect handling
    - Mock external dependencies properly

  - [ ]* 13.3 Write integration tests
    - Test complete user management workflows
    - Test role-based access control
    - Test navigation and routing behavior
    - Test API integration with real backend

- [ ] 14. Finalize implementation and integration
  - [ ] 14.1 Polish UI and user experience
    - Ensure consistent styling with existing application
    - Add proper loading states and transitions
    - Implement responsive design for mobile devices
    - Add accessibility features (ARIA labels, keyboard navigation)
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1, 8.1, 9.1, 10.1_

  - [ ] 14.2 Perform final testing and bug fixes
    - Test all user management scenarios
    - Verify security and access control
    - Test error handling and edge cases
    - Ensure proper cleanup and memory management
    - _Requirements: All requirements_