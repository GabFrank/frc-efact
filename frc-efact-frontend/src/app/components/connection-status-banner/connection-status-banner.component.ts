import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConnectionStatusService, ConnectionStatus } from '../../services/connection-status.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-connection-status-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="connection-banner" *ngIf="isOffline" [class.offline]="!connectionStatus.isOnline || !connectionStatus.isServerOnline">
      <div class="banner-content">
        <i class="fas" [class.fa-wifi]="connectionStatus.isOnline && !connectionStatus.isServerOnline" 
                      [class.fa-exclamation-triangle]="!connectionStatus.isOnline"></i>
        <span class="banner-message">
          <ng-container *ngIf="!connectionStatus.isOnline">
            Sin conexión a internet
          </ng-container>
          <ng-container *ngIf="connectionStatus.isOnline && !connectionStatus.isServerOnline">
            Servidor offline - No se puede conectar con el servidor
          </ng-container>
        </span>
      </div>
    </div>
  `,
  styles: [`
    .connection-banner {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      background: #e74c3c;
      color: white;
      padding: 12px 20px;
      z-index: 9999;
      box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.2);
      animation: slideUp 0.3s ease-out;
      pointer-events: none;
    }

    .connection-banner.offline {
      background: #e74c3c;
    }

    .banner-content {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      max-width: 1200px;
      margin: 0 auto;
      pointer-events: auto;
    }

    .banner-content i {
      font-size: 1.2rem;
    }

    .banner-message {
      font-size: 0.9rem;
      font-weight: 500;
    }

    @keyframes slideUp {
      from {
        transform: translateY(100%);
      }
      to {
        transform: translateY(0);
      }
    }

    @media (max-width: 768px) {
      .banner-content {
        flex-direction: column;
        gap: 5px;
        text-align: center;
      }

      .banner-message {
        font-size: 0.85rem;
      }
    }
  `]
})
export class ConnectionStatusBannerComponent implements OnInit, OnDestroy {
  connectionStatus: ConnectionStatus = {
    isOnline: true,
    isServerOnline: true
  };
  isOffline = false;

  private destroy$ = new Subject<void>();

  constructor(private connectionStatusService: ConnectionStatusService) {}

  ngOnInit(): void {
    this.connectionStatusService.connectionStatus$
      .pipe(takeUntil(this.destroy$))
      .subscribe(status => {
        this.connectionStatus = status;
        this.isOffline = !status.isOnline || !status.isServerOnline;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

