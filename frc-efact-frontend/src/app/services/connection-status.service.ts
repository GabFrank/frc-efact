import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, fromEvent, merge, of } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

export interface ConnectionStatus {
  isOnline: boolean;
  isServerOnline: boolean;
  lastError?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConnectionStatusService {
  private connectionStatusSubject = new BehaviorSubject<ConnectionStatus>({
    isOnline: navigator.onLine,
    isServerOnline: true
  });

  public connectionStatus$: Observable<ConnectionStatus> = this.connectionStatusSubject.asObservable();
  private http = inject(HttpClient);

  constructor() {
    // Monitorear eventos de conexión del navegador
    const online$ = fromEvent(window, 'online').pipe(map(() => true));
    const offline$ = fromEvent(window, 'offline').pipe(map(() => false));

    merge(online$, offline$)
      .pipe(startWith(navigator.onLine))
      .subscribe(isOnline => {
        const current = this.connectionStatusSubject.value;
        if (isOnline && !current.isOnline) {
          // Cuando recuperamos conexión a internet, verificamos el servidor
          // Asumimos que está online inicialmente, pero el interceptor lo actualizará si no lo está
          this.connectionStatusSubject.next({
            ...current,
            isOnline: true,
            isServerOnline: true // Optimista, el interceptor lo corregirá si es necesario
          });
        } else {
          this.connectionStatusSubject.next({
            ...current,
            isOnline,
            // Si perdemos internet, el servidor también está offline
            isServerOnline: isOnline ? current.isServerOnline : false
          });
        }
      });
  }

  /**
   * Notifica que el servidor está offline
   */
  setServerOffline(error?: string): void {
    const current = this.connectionStatusSubject.value;
    // Solo actualizar si el estado realmente cambió para evitar actualizaciones innecesarias
    if (current.isServerOnline || current.lastError !== error) {
      this.connectionStatusSubject.next({
        ...current,
        isServerOnline: false,
        lastError: error
      });
    }
  }

  /**
   * Notifica que el servidor está online
   */
  setServerOnline(): void {
    const current = this.connectionStatusSubject.value;
    // Solo actualizar si el estado realmente cambió para evitar actualizaciones innecesarias
    if (!current.isServerOnline || current.lastError !== undefined) {
      this.connectionStatusSubject.next({
        ...current,
        isServerOnline: true,
        lastError: undefined
      });
    }
  }

  /**
   * Verifica si hay algún problema de conexión
   */
  isOffline(): boolean {
    const status = this.connectionStatusSubject.value;
    return !status.isOnline || !status.isServerOnline;
  }

  /**
   * Obtiene el estado actual de conexión
   */
  getStatus(): ConnectionStatus {
    return this.connectionStatusSubject.value;
  }
}

