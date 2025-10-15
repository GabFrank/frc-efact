import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-test-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="test-page">
      <h1>Página de Prueba</h1>
      <p>Esta es una página de prueba para verificar que la navegación funciona correctamente.</p>
      <div class="test-content">
        <h2>Navegación Exitosa</h2>
        <p>Si puedes ver esta página, significa que:</p>
        <ul>
          <li>✅ El layout principal está funcionando</li>
          <li>✅ Las rutas están configuradas correctamente</li>
          <li>✅ Los guards están permitiendo el acceso</li>
          <li>✅ Los componentes se están cargando</li>
        </ul>
      </div>
    </div>
  `,
  styles: [`
    .test-page {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }

    h1 {
      color: #2c3e50;
      margin-bottom: 20px;
    }

    h2 {
      color: #3498db;
      margin-top: 30px;
      margin-bottom: 15px;
    }

    .test-content {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      border-left: 4px solid #3498db;
    }

    ul {
      margin: 15px 0;
      padding-left: 20px;
    }

    li {
      margin: 8px 0;
      font-size: 14px;
    }

    p {
      line-height: 1.6;
      color: #555;
    }
  `]
})
export class TestPageComponent {
}