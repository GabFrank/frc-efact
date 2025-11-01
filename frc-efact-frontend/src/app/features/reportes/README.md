# Reportes Feature

## Overview
The Reportes (Reports) feature provides comprehensive reporting capabilities for the FRC eFact application, allowing users to analyze invoices, clients, products, and user performance.

## Components

### ReportesListComponent
Main landing page with navigation cards to different report types.

### ReporteFacturasComponent
Invoice reporting with advanced filtering:
- Filter by company, date range, client, status, and amount
- Paginated results
- Export to Excel and PDF

### ReporteClientesComponent
Client analysis with visualization:
- Filter by company and date range
- Bar chart showing top 10 clients
- Detailed table with totals
- Summary statistics

### ReporteProductosComponent
Product sales analysis:
- Filter by company and date range
- Sortable table with product details
- Top 5 products ranking
- Summary statistics

### ReporteUsuariosComponent
User performance reporting:
- Filter by company and date range
- Sortable table with user statistics
- Top 5 users ranking
- Percentage contribution analysis

## Usage

### Navigation
Access reports from the main menu:
```
/reportes - Main reports page
/reportes/facturas - Invoice report
/reportes/clientes - Client report
/reportes/productos - Product report
/reportes/usuarios - User report
```

### Filtering
All reports support date range filtering:
1. Select empresa ID (required)
2. Optionally select date range
3. Click "Buscar" to generate report

### Exporting (Facturas only)
1. Apply desired filters
2. Click "Exportar Excel" or "Exportar PDF"
3. File will download automatically

## API Integration

Reports consume the following API endpoints:
- `GET /api/reportes/facturas` - Invoice report
- `GET /api/reportes/clientes` - Client report
- `GET /api/reportes/productos` - Product report
- `GET /api/reportes/usuarios` - User report
- `GET /api/reportes/{tipo}/excel` - Excel export
- `GET /api/reportes/{tipo}/pdf` - PDF export

## Dependencies

- Angular Material - UI components
- Chart.js - Data visualization
- RxJS - Reactive programming

## Features

- ✅ Advanced filtering
- ✅ Data visualization (charts)
- ✅ Sortable tables
- ✅ Pagination
- ✅ Export functionality
- ✅ Summary statistics
- ✅ Responsive design
- ✅ Loading states
- ✅ Error handling

## Development

### Adding a New Report

1. Create component in `src/app/features/reportes/`
2. Add route to `reportes.routes.ts`
3. Add navigation card to `reportes-list.component.ts`
4. Export component from `index.ts`

### Styling Guidelines

- Use Material Design components
- Follow existing color scheme
- Maintain consistent spacing
- Ensure responsive design

## Testing

Components are designed for easy testing:
- Standalone components
- Dependency injection
- Observable-based API calls
- Separated business logic

## Future Enhancements

- [ ] More chart types
- [ ] Real-time data refresh
- [ ] CSV export
- [ ] Print functionality
- [ ] Report scheduling
- [ ] Saved filter presets
- [ ] Period comparison
