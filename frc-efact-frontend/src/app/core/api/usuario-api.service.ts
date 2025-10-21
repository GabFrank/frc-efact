import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  User,
  CreateUserRequest,
  UpdateUserRequest,
  ResetPasswordRequest,
  Role,
  UserSearchRequest
} from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UsuarioApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/usuarios`;

  // CRUD operations
  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.baseUrl);
  }

  // Get users available for assignment to companies (excludes ADMIN)
  getAsignables(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/asignables`);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  create(user: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.baseUrl, user);
  }

  update(id: number, user: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, user);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // Search and filtering methods
  searchUsers(searchRequest: UserSearchRequest): Observable<{ content: User[], totalElements: number, totalPages: number }> {
    let params = new HttpParams();

    if (searchRequest.searchTerm) {
      params = params.set('searchTerm', searchRequest.searchTerm);
    }
    if (searchRequest.role) {
      params = params.set('role', searchRequest.role);
    }
    if (searchRequest.isActive !== undefined) {
      params = params.set('isActive', searchRequest.isActive.toString());
    }
    if (searchRequest.page !== undefined) {
      params = params.set('page', searchRequest.page.toString());
    }
    if (searchRequest.size !== undefined) {
      params = params.set('size', searchRequest.size.toString());
    }

    return this.http.get<{ content: User[], totalElements: number, totalPages: number }>(`${this.baseUrl}/buscar`, { params });
  }

  simpleSearch(term: string): Observable<User[]> {
    const params = new HttpParams().set('term', term);
    return this.http.get<User[]>(`${this.baseUrl}/search`, { params });
  }

  // Password management methods
  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/reset-password`, request);
  }

  changePassword(userId: number, oldPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${userId}/change-password`, {
      oldPassword,
      newPassword
    });
  }

  // Account management methods
  activateUser(id: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/${id}/activar`, {});
  }

  deactivateUser(id: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/${id}/desactivar`, {});
  }

  unlockUser(id: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/${id}/desbloquear`, {});
  }

  toggleUserStatus(id: number): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${id}/toggle-status`, {});
  }

  // Role management methods
  getUserRoles(userId: number): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.baseUrl}/${userId}/roles`);
  }

  assignRole(userId: number, roleId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${userId}/roles`, { roleId });
  }

  removeRole(userId: number, roleId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${userId}/roles/${roleId}`);
  }

  assignMultipleRoles(userId: number, roleIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${userId}/roles/bulk`, { roleIds });
  }

  // Statistics and monitoring
  getUserStatistics(): Observable<{
    totalUsers: number;
    activeUsers: number;
    inactiveUsers: number;
    lockedUsers: number;
  }> {
    return this.http.get<{
      totalUsers: number;
      activeUsers: number;
      inactiveUsers: number;
      lockedUsers: number;
    }>(`${this.baseUrl}/estadisticas`);
  }

  // Utility methods for filtering
  getActiveUsers(): Observable<User[]> {
    return this.searchUsers({ isActive: true }).pipe(
      map(response => response.content)
    );
  }

  getInactiveUsers(): Observable<User[]> {
    return this.searchUsers({ isActive: false }).pipe(
      map(response => response.content)
    );
  }

  getUsersByRole(role: string): Observable<User[]> {
    return this.searchUsers({ role }).pipe(
      map(response => response.content)
    );
  }

  getLockedUsers(): Observable<User[]> {
    // This would need to be implemented on the backend if not already available
    // For now, we can filter on the frontend after getting all users
    return this.getAsignables();
  }

  // Role management methods
  getAllRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${environment.apiUrl}/roles`);
  }

  // Validation methods (for async validators)
  checkUsernameAvailability(username: string, excludeUserId?: number): Observable<{ available: boolean }> {
    let params = new HttpParams().set('username', username);
    if (excludeUserId) {
      params = params.set('excludeUserId', excludeUserId.toString());
    }
    return this.http.get<{ available: boolean }>(`${this.baseUrl}/check-username`, { params })
      .pipe(
        catchError(error => {
          console.error('Error checking username availability:', error);
          // Si hay error, asumir que está disponible para no bloquear el formulario
          return of({ available: true });
        })
      );
  }

  checkEmailAvailability(email: string, excludeUserId?: number): Observable<{ available: boolean }> {
    let params = new HttpParams().set('email', email);
    if (excludeUserId) {
      params = params.set('excludeUserId', excludeUserId.toString());
    }
    return this.http.get<{ available: boolean }>(`${this.baseUrl}/check-email`, { params })
      .pipe(
        catchError(error => {
          console.error('Error checking email availability:', error);
          // Si hay error, asumir que está disponible para no bloquear el formulario
          return of({ available: true });
        })
      );
  }
}
