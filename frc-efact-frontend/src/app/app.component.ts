import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import * as AuthActions from './core/state/auth/auth.actions';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'frc-efact-frontend';
  private readonly store = inject(Store);

  ngOnInit(): void {
    // Inicializar auth desde localStorage al cargar la aplicación
    this.store.dispatch(AuthActions.initializeAuth());
  }
}
