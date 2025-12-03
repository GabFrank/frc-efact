import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Store } from '@ngrx/store';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { selectCurrentUser } from '../../core/state/auth/auth.selectors';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <div class="container">
      <mat-card *ngIf="user$ | async as user">
        <mat-card-header>
          <div mat-card-avatar>
            <mat-icon>account_circle</mat-icon>
          </div>
          <mat-card-title>{{ user.username }}</mat-card-title>
          <mat-card-subtitle>{{ user.email }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <p><strong>ID:</strong> {{ user.id }}</p>
          <p><strong>Roles:</strong> {{ getRoles(user) }}</p>
          
          <div class="auth0-section">
            <h3>Vinculación de Cuenta</h3>
            <p *ngIf="user.auth0Id">
              <mat-icon color="primary" style="vertical-align: middle;">link</mat-icon>
              Cuenta vinculada con Auth0/Google
            </p>
            <p *ngIf="!user.auth0Id">
              <mat-icon color="warn" style="vertical-align: middle;">link_off</mat-icon>
              No hay cuenta externa vinculada
            </p>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" *ngIf="!user.auth0Id" (click)="linkAccount()">
            Vincular con Auth0
          </button>
          <button mat-stroked-button color="warn" *ngIf="user.auth0Id" (click)="unlinkAccount()">
            Desvincular
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .container { padding: 20px; display: flex; justify-content: center; }
    mat-card { max-width: 600px; width: 100%; }
    .auth0-section { margin-top: 20px; padding-top: 10px; border-top: 1px solid #eee; }
  `]
})
export class UserProfileComponent {
  private store = inject(Store);
  private auth0 = inject(Auth0Service);
  private http = inject(HttpClient);

  user$ = this.store.select(selectCurrentUser);

  getRoles(user: any): string {
    if (!user.roles) return 'Sin roles';
    if (Array.isArray(user.roles)) {
        return user.roles.map((r: any) => (typeof r === 'string' ? r : r.nombre)).join(', ');
    }
    return '';
  }

  linkAccount() {
    this.auth0.loginWithPopup().subscribe({
        next: () => {
            this.auth0.user$.pipe(take(1)).subscribe(auth0User => {
                if (auth0User && auth0User.sub) {
                    this.http.post(`${environment.apiUrl}/perfil/vincular-auth0`, { auth0Id: auth0User.sub })
                        .subscribe(() => {
                            alert('Cuenta vinculada exitosamente. Por favor recargue la página.');
                            window.location.reload();
                        });
                }
            });
        },
        error: (err) => console.error('Error login popup', err)
    });
  }

  unlinkAccount() {
    if (confirm('¿Está seguro de desvincular su cuenta externa?')) {
        this.http.post(`${environment.apiUrl}/perfil/desvincular-auth0`, {})
            .subscribe(() => {
                alert('Cuenta desvinculada.');
                window.location.reload();
            });
    }
  }
}

