import { Injectable, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable, map } from 'rxjs';
import { selectCurrentUser } from '../state/auth/auth.selectors';
import { User } from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class PermissionsService {
  private store = inject(Store);
  private currentUser$ = this.store.select(selectCurrentUser);

  /**
   * Verifica si el usuario tiene alguno de los roles especificados
   */
  hasAnyRole(roles: string[]): Observable<boolean> {
    return this.currentUser$.pipe(
      map(user => {
        if (!user || !user.roles) {
          return false;
        }

        const userRoles = user.roles.map(role => 
          typeof role === 'object' && role && 'nombre' in role 
            ? (role as any).nombre 
            : role
        );

        return roles.some(requiredRole => userRoles.includes(requiredRole));
      })
    );
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  hasRole(role: string): Observable<boolean> {
    return this.hasAnyRole([role]);
  }

  // === PRODUCTOS ===
  canManageProducts(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  canViewProducts(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === CLIENTES ===
  canManageClients(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  canViewClients(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === VEHÍCULOS ===
  canManageVehicles(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  canViewVehicles(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === CHOFERES ===
  canManageDrivers(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  canViewDrivers(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === FACTURAS ===
  canManageInvoices(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  canViewInvoices(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === NOTAS DE REMISIÓN ===
  canManageDeliveryNotes(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  canViewDeliveryNotes(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === TIMBRADOS ===
  canManageTimbrados(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN']);
  }

  canViewTimbrados(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === EMPRESA ===
  canManageCompany(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN']);
  }

  canViewCompany(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === USUARIOS ===
  canManageUsers(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN']);
  }

  canViewUsers(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === DASHBOARD ===
  canViewDashboard(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === DOCUMENTOS ELECTRÓNICOS ===
  canManageElectronicDocuments(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  canViewElectronicDocuments(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === REPORTES ===
  canViewReports(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === AUDITORÍA ===
  canViewAudit(): Observable<boolean> {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  // === MÉTODOS DE UTILIDAD ===

  /**
   * Obtiene el usuario actual de forma síncrona si está disponible
   */
  getCurrentUserSync(): User | null {
    let currentUser: User | null = null;
    this.currentUser$.pipe().subscribe(user => currentUser = user).unsubscribe();
    return currentUser;
  }

  /**
   * Verifica si el usuario tiene alguno de los roles de forma síncrona
   */
  hasAnyRoleSync(roles: string[]): boolean {
    const user = this.getCurrentUserSync();
    if (!user || !user.roles) {
      return false;
    }

    const userRoles = user.roles.map(role => 
      typeof role === 'object' && role && 'nombre' in role 
        ? (role as any).nombre 
        : role
    );

    return roles.some(requiredRole => userRoles.includes(requiredRole));
  }

  /**
   * Verifica si el usuario es administrador del sistema
   */
  isSystemAdmin(): Observable<boolean> {
    return this.hasRole('ADMIN');
  }

  /**
   * Verifica si el usuario es administrador de empresa
   */
  isCompanyAdmin(): Observable<boolean> {
    return this.hasRole('EMPRESA_ADMIN');
  }

  /**
   * Verifica si el usuario es facturador
   */
  isInvoiceUser(): Observable<boolean> {
    return this.hasRole('FACTURADOR');
  }

  /**
   * Verifica si el usuario es lector
   */
  isReadOnlyUser(): Observable<boolean> {
    return this.hasRole('LECTOR');
  }
}