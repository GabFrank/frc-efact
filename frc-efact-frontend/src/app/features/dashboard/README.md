# Dashboard Feature

## Overview
The Dashboard feature provides comprehensive analytics and metrics for both users and companies in the FRC eFact system.

## Components

### DashboardUsuarioComponent
Personal dashboard showing user-specific metrics and recent activities.

**Route:** `/dashboard/usuario`

**Features:**
- Number of companies with access
- Invoices created this month
- Last access time
- Recent activities (last 10)

### DashboardEmpresaComponent
Company-specific dashboard with financial metrics and customer rankings.

**Route:** `/dashboard/empresa/:id`

**Features:**
- Total invoices issued
- Monthly sales in guaraníes
- Sales breakdown by IVA rate (10%, 5%, 0%)
- Pie chart visualization
- Top 10 customers ranking
- Date range filtering

## Reusable Components

### MetricCardComponent
```html
<app-metric-card
  label="Label"
  [value]="123"
  subtitle="Optional subtitle"
  icon="icon_name"
  iconColor="primary|accent|success|warn">
</app-metric-card>
```

### ChartCardComponent
```typescript
const config: ChartConfiguration = {
  type: 'pie',
  data: { /* chart data */ },
  options: { /* chart options */ }
};
```

```html
<app-chart-card
  title="Chart Title"
  [chartConfig]="config">
</app-chart-card>
```

### RankingListComponent
```typescript
const items: RankingItem[] = [
  { title: 'Item 1', subtitle: 'Details', value: '₲ 1.000.000' }
];
```

```html
<app-ranking-list
  title="Ranking Title"
  [items]="items"
  emptyMessage="No data">
</app-ranking-list>
```

## API Integration

### DashboardApiService

**Methods:**
- `getDashboardUsuario()`: Observable<DashboardUsuario>
- `getDashboardEmpresa(empresaId, fechaInicio?, fechaFin?)`: Observable<DashboardEmpresa>

**Example:**
```typescript
constructor(private dashboardApi: DashboardApiService) {}

loadUserDashboard() {
  this.dashboardApi.getDashboardUsuario().subscribe({
    next: (data) => console.log(data),
    error: (err) => console.error(err)
  });
}

loadCompanyDashboard(empresaId: number) {
  this.dashboardApi.getDashboardEmpresa(empresaId).subscribe({
    next: (data) => console.log(data),
    error: (err) => console.error(err)
  });
}
```

## Models

### DashboardUsuario
```typescript
interface DashboardUsuario {
  usuarioId: number;
  nombreCompleto: string;
  cantidadEmpresas: number;
  ultimoAcceso?: string;
  facturasCreadasMesActual: number;
  ultimasActividades: ActividadReciente[];
}
```

### DashboardEmpresa
```typescript
interface DashboardEmpresa {
  empresaId: number;
  razonSocial: string;
  totalFacturasEmitidas: number;
  totalGuaraniesMesActual: number;
  totalesPorIva: TotalesPorIva;
  top10Clientes: ClienteRanking[];
}
```

## Routing

```typescript
const routes = [
  { path: '', redirectTo: 'usuario', pathMatch: 'full' },
  { path: 'usuario', component: DashboardUsuarioComponent },
  { path: 'empresa/:id', component: DashboardEmpresaComponent }
];
```

## Guards
- `authGuard`: Requires authentication
- `empresaAccessGuard`: Requires company access (for empresa dashboard)

## Styling
All components use Material Design with responsive layouts:
- Desktop: Multi-column grid
- Mobile: Single column stack
- Breakpoint: 768px

## Dependencies
- Angular Material
- Chart.js 4.5.0
- RxJS

## Development

### Adding New Metrics
1. Update backend DTO
2. Update frontend model
3. Add metric card to component template
4. Update API service if needed

### Adding New Charts
1. Create ChartConfiguration object
2. Add chart-card to template
3. Update chart data in component logic

### Customizing Colors
Edit the color values in chart configurations or metric card iconColor inputs.

## Testing

Run unit tests:
```bash
npm test
```

Run e2e tests:
```bash
npm run e2e
```

## Troubleshooting

### Chart not rendering
- Ensure Chart.js is imported and registered
- Check chartConfig is properly formatted
- Verify canvas element is in DOM

### Data not loading
- Check API endpoint is correct
- Verify authentication token
- Check network tab for errors
- Ensure backend is running

### Date filter not working
- Verify date format (YYYY-MM-DD)
- Check API accepts date parameters
- Ensure dates are valid
