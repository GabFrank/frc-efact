# Facturación Module

## Quick Start

### Navigation
- List: `/facturacion`
- Create: `/facturacion/nueva`
- View: `/facturacion/:id`
- Edit: `/facturacion/:id/editar`

### Components

#### FacturaListComponent
Main list view with filtering and actions.

```typescript
import { FacturaListComponent } from './factura-list.component';
```

**Features**:
- Date range filtering
- Client search
- DE status filtering
- Actions: View, Edit, Generate DE, Print, Delete

#### FacturaFormComponent
Create and edit invoices.

```typescript
import { FacturaFormComponent } from './factura-form.component';
```

**Features**:
- Timbrado detalle selection
- Client autocomplete
- Dynamic items management
- Real-time total calculations
- IVA breakdown (0%, 5%, 10%)
- Discount application

#### FacturaItemComponent
Individual line item component.

```typescript
import { FacturaItemComponent } from './factura-item.component';
```

**Usage**:
```html
<app-factura-item
  [formGroup]="itemFormGroup"
  [index]="0"
  [productos]="productos"
  (remove)="onRemove()"
  (itemChange)="onItemChange()"
/>
```

#### FacturaViewComponent
Read-only invoice detail view.

```typescript
import { FacturaViewComponent } from './factura-view.component';
```

**Features**:
- Complete invoice display
- Items table
- Totals breakdown
- Actions: Edit, Generate DE, Print

### API Integration

```typescript
// List invoices
facturaApi.getAll(filtro).subscribe(facturas => {
  // Handle facturas
});

// Get single invoice
facturaApi.getById(id).subscribe(factura => {
  // Handle factura
});

// Create invoice
facturaApi.create(facturaData).subscribe(factura => {
  // Handle created factura
});

// Update invoice
facturaApi.update(id, facturaData).subscribe(factura => {
  // Handle updated factura
});

// Generate DE
facturaApi.generarDE(facturaId).subscribe(de => {
  // Handle generated DE
});

// Download PDF
facturaApi.descargarPDF(facturaId).subscribe(blob => {
  // Handle PDF blob
});
```

### Form Structure

```typescript
{
  empresaId: number,
  timbradoDetalleId: number,      // Required
  clienteId: number,
  fecha: Date,                     // Required
  credito: boolean,
  nombre: string,                  // Required, max 200
  ruc: string,                     // Max 20
  direccion: string,               // Max 500
  items: [                         // Min 1 item
    {
      productoId: number,
      cantidad: number,            // Required, min 0.001
      descripcion: string,         // Required, max 500
      precioUnitario: number,      // Required, min 0
      total: number                // Calculated
    }
  ],
  descuentoFinal: number,          // Min 0
  // Calculated fields
  ivaParcial0: number,
  ivaParcial5: number,
  ivaParcial10: number,
  totalParcial0: number,
  totalParcial5: number,
  totalParcial10: number,
  totalParcial: number,
  totalFinal: number
}
```

### Permissions

- **View**: All authenticated users
- **Create**: ADMIN, EMPRESA_ADMIN, FACTURADOR
- **Edit**: ADMIN, EMPRESA_ADMIN, FACTURADOR
- **Delete**: ADMIN, EMPRESA_ADMIN

### Styling

All components use Material Design with consistent styling:
- Primary color: #1976d2
- 8px grid system
- Responsive layouts
- Mobile-first approach

### Common Tasks

#### Add a new filter
1. Add form control to `FacturaListComponent`
2. Update `filtro` object
3. Modify `aplicarFiltros()` method
4. Add UI element in template

#### Customize totals calculation
1. Modify `calcularTotales()` in `FacturaFormComponent`
2. Update `totales` computed signal
3. Adjust display in template

#### Add new action button
1. Add action to `tableActions` array
2. Handle in `onActionClick()` method
3. Implement action method

### Troubleshooting

**Issue**: Totals not updating
- **Solution**: Ensure `recalcularTotales()` is called after item changes

**Issue**: Client autocomplete not working
- **Solution**: Check `setupClienteAutocomplete()` debounce time and API call

**Issue**: Timbrado shows no numbers available
- **Solution**: Verify `numeroActual < rangoHasta` in timbrado detalle

**Issue**: Form validation errors not showing
- **Solution**: Ensure `form.markAllAsTouched()` is called on submit

### Testing

```typescript
// Example unit test
describe('FacturaFormComponent', () => {
  it('should calculate totals correctly', () => {
    // Test implementation
  });
  
  it('should validate required fields', () => {
    // Test implementation
  });
});
```

### Future Enhancements
- [ ] Invoice templates
- [ ] Recurring invoices
- [ ] Bulk operations
- [ ] Advanced search
- [ ] Export functionality
- [ ] Email integration
- [ ] Mobile optimization

## Support

For issues or questions, refer to:
- [Implementation Summary](../../../FACTURACION_FEATURE_IMPLEMENTATION_SUMMARY.md)
- [Design Document](../../../../../.kiro/specs/electronic-invoicing-system/design.md)
- [Requirements](../../../../../.kiro/specs/electronic-invoicing-system/requirements.md)
